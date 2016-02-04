package com.synload.csumb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Calender extends FragmentActivity  {
    public static Calendar cal;
    public static CaldroidFragment caldroidFragment;
    public static LinearLayout header;
    public static Calender current;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);
        current=this;
        caldroidFragment = new CaldroidFragment();
        header = (LinearLayout) findViewById(R.id.Calender_header);
        Bundle args = new Bundle();
        cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                if(CSUMBAPI.assignments.containsKey(date.toString())) {
                    ArrayList<Assignment> asses = CSUMBAPI.assignments.get(date.toString());
                    String output = "";
                    for(Assignment ass: asses){
                        output+="\n"+ass.clazz;
                    }
                    Toast.makeText(getApplicationContext(), asses.size()+" Assignments Due!"+output, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChangeMonth(int month, int year) {

            }

            @Override
            public void onLongClickDate(Date date, View view) {
                if(CSUMBAPI.assignments.containsKey(date.toString())){
                    Bundle b = new Bundle();
                    b.putString("date", date.toString());
                    Intent i = new Intent(Calender.current, Details.class);
                    i.putExtras(b);
                    startActivity(i);
                }
            }

            @Override
            public void onCaldroidViewCreated() {

            }

        };
        caldroidFragment.setCaldroidListener(listener);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calender, caldroidFragment);
        t.commit();
        CSUMBAPI.init();
        if(this.getIntent().getExtras()!=null && this.getIntent().getExtras().size()>0){
            if(this.getIntent().getExtras().containsKey("username")){
                CSUMBAPI.login(this.getIntent().getExtras().getString("username"), this.getIntent().getExtras().getString("password"));
            }
        }else{
            CSUMBAPI.getClasses();
        }
        Button m = (Button) findViewById(R.id.logout);
        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CSUMBAPI.logout();
                Calender.current.startActivity(new Intent(Calender.current, LoginActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calender, menu);
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
