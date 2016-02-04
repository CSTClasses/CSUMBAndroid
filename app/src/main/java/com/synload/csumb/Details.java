package com.synload.csumb;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class Details extends Activity {
    public static WebView wv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        wv = (WebView)findViewById(R.id.webView);
        String html = "<table style='width:100%;'><tr><th>Assignment</th></tr>";
        for(Assignment ass: CSUMBAPI.assignments.get(this.getIntent().getExtras().getString("date"))){
            html+="<tr><td>"+ass.clazz+"</td></tr>";
            html+="<tr><td>"+ass.title+"</td></tr>";
            html+="<tr><td><a href=\"https://ilearn.csumb.edu/mod/assign/view.php?id="+ass.id+"\">Go to assignment</a></td></tr>";
            html+="<tr><td>"+ass.description+"</td></tr>";
            html+="<tr><td><hr/></td></tr>";
        }
        wv.loadData(html+"</table>", "text/html", "utf-8");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
