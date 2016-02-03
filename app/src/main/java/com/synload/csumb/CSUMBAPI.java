package com.synload.csumb;

import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import java.text.SimpleDateFormat;
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
    public static Map<String, String> classes = new HashMap<String, String>();
    public static void getClasses(){
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
                    if(!ResultString.equals("")) {
                        try {
                            regex = Pattern.compile("title=\"(.*?)\" href=\"https://ilearn\\.csumb\\.edu/course/view\\.php\\?id=([0-9]+)\"");
                            regexMatcher = regex.matcher(ResultString);
                            while (regexMatcher.find()) {
                                classes.put(regexMatcher.group(2),regexMatcher.group(1));
                            }
                        } catch (PatternSyntaxException ex) {
                        }
                    }
                    CSUMBAPI.getAssignments();
                } catch (PatternSyntaxException ex) {}
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {}
        });
    }
    public static void loadAssignment(String url){
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String data = new String(response);
                try {
                    Pattern regex = Pattern.compile("<td class=\"(.*?)\" style=\"\">Due date</td>(?s:.*?)<td class=\"(.*?)\" style=\"\">(.*?)</td>");
                    Matcher regexMatcher = regex.matcher(data);
                    if (regexMatcher.find()) {
                        //Calender.calenderView
                        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm a");
                        try{
                            System.out.println(regexMatcher.group(3));
                            Date d = formatter.parse(regexMatcher.group(3));
                            System.out.println(d.toString());
                            Calender.caldroidFragment.setBackgroundResourceForDate(R.color.caldroid_light_red, d );
                        }catch(Exception e){
                        e.printStackTrace();}

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
        for(Map.Entry<String, String> classData :classes.entrySet()){
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
                                    loadAssignment(regexMatchers.group());
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
    public static void login(final String username, final String password){
        client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        // Initialize Variables
        PersistentCookieStore myCookieStore = new PersistentCookieStore(Calender.current);
        client.setCookieStore(myCookieStore);
        client.post("https://sso.csumb.edu/cas/login?service=https%3A%2F%2Filearn.csumb.edu%2Flogin%2Findex.php%3FauthCAS%3DCAS", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String data = new String(response);
                RequestParams params = new RequestParams();
                try {
                    Pattern regex = Pattern.compile("name=\"(.*?)\" value=\"(.*?)\"");
                    Matcher regexMatcher = regex.matcher(data);
                    while (regexMatcher.find()) {
                        String key = regexMatcher.group(1);
                        String val = regexMatcher.group(2);
                        if(key.equalsIgnoreCase("lt") || key.equalsIgnoreCase("execution")){
                            params.add(key, val);
                            System.out.println("findMe 2: "+key+"="+val);
                        }
                    }
                } catch (PatternSyntaxException ex) {}
                params.add("username",username);
                params.add("password",password);
                params.add("_eventId","submit");
                params.add("submit","Log In");
                client.post("https://sso.csumb.edu/cas/login?service=https%3A%2F%2Filearn.csumb.edu%2Flogin%2Findex.php%3FauthCAS%3DCAS", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        //String data = new String(response);
                        //Calender.textViewToChange.setText(data);
                        CSUMBAPI.getClasses();
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {}
                });
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {}
        });
    }
}


