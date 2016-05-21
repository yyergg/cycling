package com.chasewind.cycling;

import android.content.pm.ActivityInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String APP_TAG = "cycling";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private FragmentConnect mFragmentConnect;
    private FragmentData mFragmentData;

    public BLEManager mBLEManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(APP_TAG,"onCreated");
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mBLEManager = new BLEManager(this);

        if (getSupportFragmentManager().findFragmentByTag(getFragmentTag(0)) != null) {
            String tag_0 = getFragmentTag(0);
            mFragmentConnect = (FragmentConnect) getSupportFragmentManager().findFragmentByTag(getFragmentTag(0));
            mFragmentData = (FragmentData) getSupportFragmentManager().findFragmentByTag(getFragmentTag(1));
            Log.d(APP_TAG, "get frag done");
        } else {
            mFragmentConnect = FragmentConnect.getInstance(1);
            mFragmentData = FragmentData.getInstance(2);
            Log.d(APP_TAG, "init frag done");
        }



        Log.d(APP_TAG, "before init adapter");
        // Set up the ViewPager with the sections adapter.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Log.d(APP_TAG, "init adapter done");
    }

    private String getFragmentTag(int fragmentPosition)
    {
        return "android:switcher:" + Integer.toString(R.id.container) + ":" + fragmentPosition;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(APP_TAG,"getitem");
//            Thread.dumpStack();
            switch (position) {
                case 0:
                    return mFragmentConnect;
                case 1:
                    return mFragmentData;
//                case 2:
//                    return mFragmentConnect;
                default:
                    return mFragmentConnect;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Connect Sensor";
                case 1:
                    return "Sensor Data";
                case 2:
                    return "HRV";
            }
            return null;
        }
    }
}
