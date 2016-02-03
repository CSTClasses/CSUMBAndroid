package com.synload.csumb;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import com.roomorama.caldroid.CaldroidFragment;

import java.util.Calendar;

public class Calender extends FragmentActivity  {
    public static Calendar cal;
    public static CaldroidFragment caldroidFragment;
    public static Calender current;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);
        current=this;
        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calender, caldroidFragment);
        t.commit();
        CSUMBAPI.login("user", "pass");
        CSUMBAPI.getClasses();
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
