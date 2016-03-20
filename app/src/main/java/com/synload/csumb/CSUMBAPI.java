package com.synload.csumb;

import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.loopj.android.http.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Nathaniel on 2/2/2016.
 */
public class CSUMBAPI {
    public static AsyncHttpClient client;
    public static boolean isLoggedIn=false;
    public static PersistentCookieStore myCookieStore;
    public static Map<Date, Integer> assignmentStatus = new HashMap<Date, Integer>();
    public static Map<String, String> classes = new HashMap<String, String>();
    public static Map<String, ArrayList<Assignment>> assignments = new HashMap<String, ArrayList<Assignment>>();
    public static void logout(){
        assignments = new HashMap<String, ArrayList<Assignment>>();
        assignmentStatus = new HashMap<Date, Integer>();
        classes = new HashMap<String, String>();
        isLoggedIn = false;
        myCookieStore.clear();
        client.get("https://sso.csumb.edu/cas/logout", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
            }
        });
    }
    public static void getClasses(){
        isLoggedIn = true;
        client.get("https://ilearn.csumb.edu", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String data = new String(response);

                String ResultString = null;
                try {
                    Pattern regex = Pattern.compile("<span>My courses</span>(?s:.*?)<ul>(?s:.*?)</ul>(?s:.*?)</li>");
                    Matcher regexMatcher = regex.matcher(data);
                    if (regexMatcher.find()) {
                        ResultString = regexMatcher.group();
                    }
                    if(ResultString!=null) {
                        try {
                            regex = Pattern.compile("title=\"(.*?)\" href=\"https://ilearn\\.csumb\\.edu/course/view\\.php\\?id=([0-9]+)\"");
                            regexMatcher = regex.matcher(ResultString);
                            while (regexMatcher.find()) {
                                classes.put(regexMatcher.group(2),regexMatcher.group(1));
                            }
                        } catch (PatternSyntaxException ex) {
                        }
                        CSUMBAPI.getAssignments();
                        Calender.current.setTitle("CSUMB Classes "+classes.size());

                    }
                } catch (PatternSyntaxException ex) {}
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {}
        });
    }
    public static void loadAssignment(String url, final Map.Entry<String, String> classData){
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String data = new String(response);
                try {
                    Pattern regex = Pattern.compile("<td class=\"(.*?)\" style=\"\">Due date</td>(?s:.*?)<td class=\"(.*?)\" style=\"\">(.*?)</td>");
                    Matcher regexMatcher = regex.matcher(data);
                    if (regexMatcher.find()) {
                        //Calender.calenderView
                        SimpleDateFormat timeFormatter = new SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm a");
                        SimpleDateFormat dayFormatter = new SimpleDateFormat("EEEE, dd MMM yyyy");
                        try{
                            //System.out.println("findMe: "+regexMatcher.group(3));
                            Date d = dayFormatter.parse(regexMatcher.group(3).replaceAll("(?s), ([0-9]+):([0-9]+) (AM|PM)", ""));
                            Date t = timeFormatter.parse(regexMatcher.group(3));
                            //System.out.println(d.toString());
                            boolean foundMatch = false;
                            try {
                                Pattern regexc = Pattern.compile("Submitted for grading", Pattern.DOTALL);
                                Matcher regexMatcherc = regexc.matcher(data);
                                foundMatch = regexMatcherc.find();
                            } catch (PatternSyntaxException ex) {
                            }
                            if(foundMatch){
                                if(assignmentStatus.containsKey(d)){
                                    if(assignmentStatus.get(d) == 0) {
                                        Calender.caldroidFragment.setBackgroundResourceForDate(R.color.Orange, d);
                                        assignmentStatus.put(d, 2);
                                    }else if(assignmentStatus.get(d) == 1) {
                                        // keep green
                                    }
                                }else{
                                    Calender.caldroidFragment.setBackgroundResourceForDate(R.color.Green, d);
                                    assignmentStatus.put(d,1);
                                }
                            }else{
                                if(assignmentStatus.containsKey(d) && assignmentStatus.get(d) == 1) {
                                    Calender.caldroidFragment.setBackgroundResourceForDate(R.color.Orange, d);
                                    assignmentStatus.put(d,2);
                                }else{
                                    Calender.caldroidFragment.setBackgroundResourceForDate(R.color.Red, d);
                                    assignmentStatus.put(d,0);
                                }
                            }

                            Calender.caldroidFragment.refreshView();
                            String title = null;
                            String id = null;
                            String description = null;
                            try {
                                regex = Pattern.compile("<a title=\"Assignment\" href=\"https://ilearn\\.csumb\\.edu/mod/assign/view\\.php\\?id=([0-9]+)\">(.*?)</a></li></ul></nav>", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
                                regexMatcher = regex.matcher(data);
                                if (regexMatcher.find()) {
                                    title = regexMatcher.group(2);
                                    id = regexMatcher.group(1);
                                }
                            } catch (PatternSyntaxException ex) {}
                            try {
                                regex = Pattern.compile("<div class=\"no-overflow\">(.*?)</div>", Pattern.DOTALL);
                                regexMatcher = regex.matcher(data);
                                if (regexMatcher.find()) {
                                    description = regexMatcher.group(1);
                                }
                            } catch (PatternSyntaxException ex) {}
                            if(assignments.containsKey(d.toString())){
                                assignments.get(d.toString()).add(new Assignment(title, description, classData.getValue(), t, id ));
                            }else{
                                assignments.put(d.toString(), new ArrayList<Assignment>());
                                assignments.get(d.toString()).add(new Assignment(title, description, classData.getValue(), t, id));
                            }
                            /*for(Map.Entry<String, ArrayList<Assignment>> ass: assignments.entrySet()){
                                System.out.println("findMe: "+ass.getKey());
                                for(Assignment a: ass.getValue()){
                                    System.out.println("findMe: \t\t"+a.title + " class: "+a.clazz);
                                }
                            }*/
                        }catch(Exception e){}
                    }
                } catch (PatternSyntaxException ex) {
                    // Syntax error in the regular expression
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {}
        });
    }
    public static void getAssignments(){
        assignments = new HashMap<String, ArrayList<Assignment>>();
        assignmentStatus = new HashMap<Date, Integer>();
        for(final Map.Entry<String, String> classData :classes.entrySet()){
            client.get("https://ilearn.csumb.edu/mod/assign/index.php?id="+classData.getKey(), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    String data = new String(response);
                    try {
                        Pattern regex = Pattern.compile("<tr(?s:.*?)</tr>");
                        Matcher regexMatcher = regex.matcher(data);
                        while (regexMatcher.find()) {
                            try {
                                Pattern regexs = Pattern.compile("https://ilearn\\.csumb\\.edu/mod/assign/view\\.php\\?id=([0-9]+)");
                                Matcher regexMatchers = regexs.matcher(regexMatcher.group());
                                while (regexMatchers.find()) {
                                    loadAssignment(regexMatchers.group(), classData);
                                }
                            } catch (PatternSyntaxException ex) {}
                        }
                    } catch (PatternSyntaxException ex) {}
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {}
            });
        }
    }
    public static void init(){
        client = new AsyncHttpClient();
        myCookieStore = new PersistentCookieStore(Calender.current);
        client.setCookieStore(myCookieStore);
    }
    public static void login(final String username, final String password){
        RequestParams params = new RequestParams();
        client.get("https://sso.csumb.edu/cas/login?service=https%3A%2F%2Filearn.csumb.edu%2Flogin%2Findex.php%3FauthCAS%3DCAS", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String data = new String(response);
                //System.out.println(data);
                RequestParams params = new RequestParams();
                try {
                    Pattern regex = Pattern.compile("name=\"(.*?)\" value=\"(.*?)\"");
                    Matcher regexMatcher = regex.matcher(data);
                    while (regexMatcher.find()) {
                        String key = regexMatcher.group(1);
                        String val = regexMatcher.group(2);
                        if (key.equalsIgnoreCase("lt") || key.equalsIgnoreCase("execution")) {
                            params.add(key, val);
                            //System.out.println("findMe 2: " + key + "=" + val);
                        }
                    }
                } catch (PatternSyntaxException ex) {}
                if (params.has("execution")) {
                    params.add("username", username);
                    params.add("password", password);
                    params.add("_eventId", "submit");
                    params.add("submit", "Log In");
                    client.post("https://sso.csumb.edu/cas/login?service=https%3A%2F%2Filearn.csumb.edu%2Flogin%2Findex.php%3FauthCAS%3DCAS", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                            String data = new String(response);
                            //Calender.textViewToChange.setText(data);
                            //System.out.println(data);
                            CSUMBAPI.getClasses();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                            //String data = new String(errorResponse);
                            if(statusCode==302){
                                String location= null;
                                for(Header h: headers){
                                    System.out.println("findMe 3:"+h.getName()+" = "+h.getValue());
                                    if(h.getName().equalsIgnoreCase("location")){
                                        location = h.getValue();
                                    }
                                }
                                if(location!=null) {
                                    client.get(location, new AsyncHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                                            CSUMBAPI.getClasses();
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                                            //String data = new String(errorResponse);
                                            if (statusCode == 302) {
                                                CSUMBAPI.getClasses();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    CSUMBAPI.getClasses();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                if (statusCode == 302) {
                    CSUMBAPI.getClasses();
                }
            }
        });
    }
}
