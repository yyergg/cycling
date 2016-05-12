package com.chasewind.cycling;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class FragmentConnect extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private BLEManager mBLEManager;
    private ListView mLvHRList;
    private List mScannedDevices;

    public static FragmentConnect newInstance(int sectionNumber) {
        FragmentConnect fragment = new FragmentConnect();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentConnect() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);
        this.mLvHRList = (ListView) rootView.findViewById(R.id.lvHRList);
        Log.d("connect",this.mLvHRList.toString());
        this.mBLEManager = ((MainActivity)this.getActivity()).mBLEManager;

        mBLEManager.scanBLEDevice();

        final Handler handler = new Handler();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(mBLEManager.mScanning) {
                        sleep(1000);
                    }
                    mScannedDevices = mBLEManager.mScannedDevices;
                    handler.post(new Runnable() {
                        public void run() {
                            printDevices();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        String[] scanning = {"Scanning....","Please wait"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this.getActivity(),
                R.layout.list_device,
                scanning);
        this.mLvHRList.setAdapter(arrayAdapter);
        return rootView;
    }

    private void printDevices(){
        ArrayList<String> devices = new ArrayList<>();
        int i;
        for(i=0;i<mScannedDevices.size();i++){
            devices.add(((BluetoothDevice)mScannedDevices.get(i)).getName()+"\n"+((BluetoothDevice)mScannedDevices.get(i)).getAddress());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this.getActivity(),
                R.layout.list_device,
                devices);

        this.mLvHRList.setAdapter(arrayAdapter);
    }
}
