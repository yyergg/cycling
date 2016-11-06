package com.chasewind.cycling;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by yyerg on 2016/5/13.
 */
public class FragmentData extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private BLEManager mBLEManager;
    private static FragmentData instance;
    private TextView tvHeartRate;
    private TextView tvRPM;

    public static FragmentData getInstance(int sectionNumber) {
        if(instance == null) {
            instance = new FragmentData();
        }
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        instance.setArguments(args);
        return instance;
    }

    public FragmentData() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_data, container, false);
        this.mBLEManager = ((MainActivity)this.getActivity()).mBLEManager;
        this.tvHeartRate = (TextView)rootView.findViewById(R.id.tvHeartRate);
        this.tvRPM = (TextView)rootView.findViewById(R.id.tvRPM);
        final Handler handler = new Handler();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(1000);
                        handler.post(new Runnable() {
                            public void run() {
                                Log.d("data", mBLEManager.HR_amount.toString());
                                printHR(mBLEManager.HR_amount);
                                printRPM(mBLEManager.RPM_amount);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        final ScheduledFuture httpHandle =
                scheduleTaskExecutor.scheduleAtFixedRate(httpThread, 5, 5, TimeUnit.SECONDS);
        return rootView;
    }

    private void printHR(Integer HR_amount){
        if(HR_amount == null || HR_amount==0 ){
            tvHeartRate.setText("---");
        } else {
            tvHeartRate.setText(HR_amount.toString());
        }
    }
    private void printRPM(Integer RPM_amount){
        if(RPM_amount == null || RPM_amount==0 ){
            tvRPM.setText("---");
        } else {
            tvRPM.setText(RPM_amount.toString());
        }
    }

    private Runnable httpThread = new Runnable() {
        public void run() {
            sendPostDataToInternet();
        }
    };

    private void sendPostDataToInternet() {
        HttpPost httpRequest = new HttpPost("http://52.196.218.0/chasewind_web/android.php");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        try {
            params.add(new BasicNameValuePair("username", "William Su"));
            params.add(new BasicNameValuePair("type", "heartrate"));
            params.add(new BasicNameValuePair("value", mBLEManager.HR_amount.toString()));
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String strResult = EntityUtils.toString(httpResponse.getEntity());
                Log.d("SQL",strResult);
            }
        }catch(Exception e){
            Log.d("SQL",e.toString());
        }
        HttpPost httpRequest2 = new HttpPost("http://52.196.218.0/chasewind_web/android.php");
        List<NameValuePair> params2 = new ArrayList<NameValuePair>();
        try {
            params2.add(new BasicNameValuePair("username", "William Su"));
            params2.add(new BasicNameValuePair("type", "RPM"));
            params2.add(new BasicNameValuePair("value", mBLEManager.RPM_amount.toString()));
            httpRequest.setEntity(new UrlEncodedFormEntity(params2, HTTP.UTF_8));
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String strResult = EntityUtils.toString(httpResponse.getEntity());
                Log.d("SQL",strResult);
            }
        }catch(Exception e){
            Log.d("SQL",e.toString());
        }
    }
}
