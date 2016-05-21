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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yyerg on 2016/5/4.
 */
public class BLEManager {
    public static String HEART_RATE_MEASUREMENT = "00002a37";

    public Integer HR_amount = 0;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private Activity mActivity;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mHRGatt;
    private Integer mConnectionState;
    private final Integer REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    public boolean mScanning;
    public boolean mConnected;
    public ArrayList mScannedDevices;
    public ArrayList mConnectedDevices;
    //public BluetoothDevice

    public List<BluetoothGattService> mLeServices;

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
                if(!mScannedDevices.contains(device)) {
                    mScannedDevices.add(device);
                }
            }
        };

    public boolean connectHR(BluetoothDevice device) {
        Log.d("BLE", "connectHR");
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
        mHRGatt = device.connectGatt(mActivity, false, mHRGattCallback);
        return true;
    }

    // connection change and services discovered.
    private final BluetoothGattCallback mHRGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.d("BLE", "Connected to HR.");
                mHRGatt.discoverServices();
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
                mLeServices = mHRGatt.getServices();
                Integer i;
                for(i=0;i<mLeServices.size();i++){
                    if(((BluetoothGattService)mLeServices.get(i)).getUuid().toString().substring(0,8).equals("0000180d")) {
                        mLeHeartRateService = mLeServices.get(i);
                        break;
                    }
                }

                List<BluetoothGattCharacteristic> mLeCharacteristic;
                BluetoothGattCharacteristic mLeHeartRateCharacteristic = null;
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
                mHRGatt.writeDescriptor(descriptor);
                mHRGatt.setCharacteristicNotification(mLeHeartRateCharacteristic, true);
                if(!mHRGatt.readCharacteristic(mLeHeartRateCharacteristic)){
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


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mActivity.sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.
        // Data parsing is carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (characteristic.getUuid().toString().substring(0,8).equals(HEART_RATE_MEASUREMENT)) {
            int dataFlags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            int heartRateMeasurementValue = 0;
            boolean heartRateValueFormat = false; // UINT8 or UINT16
            boolean sensorContactSupportedStatus = false; // "not supported" or "supported"
            boolean sensorContactDetectedStatus = false; // "not detected" or "detected"
            boolean energyExpendedStatus = false; // "not present" or "not present"
            boolean RrInterval = false; //"not present" or "one or more values are present. unit 1/1024s"
            heartRateValueFormat = ((dataFlags & 1) != 0);
            sensorContactSupportedStatus = ((dataFlags>>2 & 1) != 0);
            sensorContactDetectedStatus = ((dataFlags>>1 & 1) != 0);
            energyExpendedStatus = ((dataFlags>>3 & 1) != 0);
            RrInterval = ((dataFlags>>4 & 1) != 0);

            if (heartRateValueFormat) {
                heartRateMeasurementValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
            } else {
                heartRateMeasurementValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
            }
            final byte[] data = ByteBuffer.allocate(4).putInt(heartRateMeasurementValue).array();
            Log.d("BLE", String.format("Received heart rate: %d", heartRateMeasurementValue));
            HR_amount = heartRateMeasurementValue;
            intent.putExtra(ACTION_DATA_AVAILABLE,data);
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        mActivity.sendBroadcast(intent);
    }

}
