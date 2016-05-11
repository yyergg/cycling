package com.chasewind.cycling;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yyerg on 2016/5/4.
 */
public class BLEManager {
    public static String HEART_RATE_MEASUREMENT = "00002a37";

    private Activity mActivity;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Integer mConnectionState;
    private final Integer REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;
    private ArrayList mScannedDevices;
    private ArrayList mConnectedDevices;

    private List<BluetoothGattService> mLeServices;

    private BluetoothGattService mLeHeartRateService;

    private Handler mHandler;

    public BLEManager(Activity activity){
        mActivity = activity;
        this.mBluetoothManager =
                (BluetoothManager) mActivity.getSystemService(mActivity.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = mBluetoothManager.getAdapter();

        mConnectedDevices = new ArrayList<BluetoothDevice>();
        mScannedDevices = new ArrayList<BluetoothDevice>();

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
        mScannedDevices.clear();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mBLEScanCallback);
            }
        }, SCAN_PERIOD);
        mScanning = true;
        mBluetoothAdapter.startLeScan(mBLEScanCallback);
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mBLEScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.d("BLE", device.toString());
                if(!mScannedDevices.contains(device)) {
                    mScannedDevices.add(device);
                }
            }
        };

    public boolean connect(BluetoothDevice device) {
        if (mBluetoothAdapter == null) {
            Log.d("BLE", "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (device == null) {
            Log.d("BLE", "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mActivity, false, mGattCallback);
        return true;
    }

    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.d("BLE", "Connected to device.");
                mBluetoothGatt.discoverServices();
                // Attempts to discover services after successful connection.
                Log.i("BLE", "Attempting to start service discovery");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i("BLE", "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mLeServices = mBluetoothGatt.getServices();
                Integer i;
                for(i=0;i<mLeServices.size();i++){
                    if(()mLeServices.get(i).getUuid().toString().substring(0,8).equals("0000180d")) {
                        mLeHeartRateService = mLeServices.get(i);
                        break;
                    }
                }
                mLeCharacteristic = mLeHeartRateService.getCharacteristics();
                for(i=0;i<mLeCharacteristic.size();i++){
                    if(mLeCharacteristic.get(i).getUuid().toString().substring(0,8).equals("00002a37")) {
                        mLeHeartRateCharacteristic = mLeCharacteristic.get(i);
                        break;
                    }
                }

                Log.d("BLE","***********"+mLeHeartRateCharacteristic.getUuid());
                List<BluetoothGattDescriptor> mLeDescriptor;
                mLeDescriptor = mLeHeartRateCharacteristic.getDescriptors();

                for(i=0;i<mLeDescriptor.size();i++){
                    Log.d("BLE",mLeDescriptor.get(i).getUuid().toString());
                }

                BluetoothGattDescriptor descriptor = mLeHeartRateCharacteristic.getDescriptor(mLeDescriptor.get(0).getUuid());
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
                mBluetoothGatt.setCharacteristicNotification(mLeHeartRateCharacteristic, true);
                if(!mBluetoothGatt.readCharacteristic(mLeHeartRateCharacteristic)){
                    Log.d("BLE","read");
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w("BLE", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            Log.d("BLE", "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            Log.d("BLE", "onCharacteristicChanged");
        }
    };
}
