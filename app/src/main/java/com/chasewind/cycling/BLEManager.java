package com.chasewind.cycling;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by yyerg on 2016/5/4.
 */
public class BLEManager {
    public static String HEART_RATE_MEASUREMENT = "00002a37";

    private Activity mActivity;

    private BluetoothManager mbluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Integer mConnectionState;
    private final Integer REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 100000;
    private boolean mScanning;
    private ArrayList mScannedBLEDevices = new ArrayList<BluetoothDevice>();
    private ArrayList mConnectedBLEDevices = new ArrayList<BluetoothDevice>();
    private BluetoothGattService mLeHeartRateService;

    private Handler mHandler;

    public BLEManager(Activity activity){
        mActivity = activity;
        this.mbluetoothManager =
                (BluetoothManager) mActivity.getSystemService(mActivity.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = mbluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            mActivity.finish();
            return;
        }
        mHandler = new Handler();
    }

    public void scanBLEDevice(){
        Log.d("AAAA","!!!!!!!!!!!!!1");
        mScannedBLEDevices.clear();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mScanning = false;
//                mBluetoothAdapter.stopLeScan(mBLEScanCallback);
//            }
//        }, SCAN_PERIOD);
        mScanning = true;
        mBluetoothAdapter.startLeScan(mBLEScanCallback);
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mBLEScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.d("BLE", device.toString());
                if(!mScannedBLEDevices.contains(device)) {
                    mScannedBLEDevices.add(device);
                }
            }
        };
}
