package com.omri.bralliant;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;


public class Bluetooth {
    private final Context mContext;
    private final MainActivity mActivity;
    final String devcAddress = "D8:A0:1D:51:B4:9A"; //MyDeice
    //final String devcAddress = "D8:A0:1D:55:A7:46"; //MyDeice
    final String devcName = "Bralliant";
    private static final UUID ServiceUUID = UUID.fromString("801a272c-a57b-11ea-bb37-0242ac130002");
    private static final UUID CharUUID = UUID.fromString("801a29a2-a57b-11ea-bb37-0242ac130002");
//    private static final UUID ServiceUUID = UUID.fromString("eb1d0c5f-01f0-45e9-ba6d-9a7902b7c3c3");
//    private static final UUID CharUUID = UUID.fromString("e7e7691a-6b80-4ca9-9973-59ef68253f44");
    private String scanDevcAddress = null;
    private String scanDevcName = null;
    private BluetoothAdapter mBTAdapter;
    private static final long SCAN_PERIOD = 5000;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothDevice BTdevice;
    private BluetoothGatt mBTGatt;
    private BluetoothGattCharacteristic mGattChar;
    private BluetoothGattService mGattService;
    String[] stringArray;
    Handler mHandler;

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

    Bluetooth(Context context, MainActivity activity) {
        this.mContext = context;
        this.mActivity = activity;
        mHandler = new Handler();
        disconnect();
        close();
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBTAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            Log.d("ADebugTag", "mBluetoothLeScanner is null");
        }
        if (mBTAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(mContext, "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        } else {
            if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(mContext, "no BLE feature in phone", Toast.LENGTH_SHORT).show();
            }

            searchDevice();

            //connect();

        }

    }

    public void turnOnBTAdapter(){
        mBTAdapter.enable();
    }

    public void searchDevice() {
        if (mBTAdapter.isEnabled()) {
            scanLeDevice(true);
        } else {
            Toast.makeText(mContext, "To begin please turn on bluetooth", Toast.LENGTH_SHORT).show();
            mActivity.setBTwarning();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            Log.d("ADebugTag", "Just before scan");
            mBluetoothLeScanner.startScan(leScanCallback);
        } else {
            mBluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("ADebugTag", "Inside scan callback scan");
            BTdevice = result.getDevice();
            scanDevcName = result.getDevice().getName();
            scanDevcAddress = result.getDevice().getAddress();
            Log.d("ADebugTag", "Scanned address: " + scanDevcAddress);
            Log.d("ADebugTag", "Scanned NAME: " + scanDevcName);
            if (scanDevcName == null){
                return;
            }
            else if (scanDevcName.equals(devcName)) {
                mActivity.updatesStatus(MainActivity.DEVICE_FOUND,"");
                scanLeDevice(false);
                Log.d("ADebugTag", "Found bralliant!");
                //connect();
            }
        }
    };

    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch (Exception localException) {
            Log.d("ADebugTag", "An exception occured while refreshing device");
        }
        return false;
    }

    public boolean connect() {
        //mActivity.updatesStatus(MainActivity.STATE_CONNECTING,"");
        BTdevice = mBTAdapter.getRemoteDevice(devcAddress);
        if (Build.VERSION.SDK_INT >= 23)
            mBTGatt = BTdevice.connectGatt(mContext, true, mGattCallback,BTdevice.TRANSPORT_LE);
        else {
            mBTGatt = BTdevice.connectGatt(mContext,true, mGattCallback);
        }
        refreshDeviceCache(mBTGatt);
        if (mBTGatt == null){
            Log.d("ADebugTag", "mBTGatt is null");
            return false;
        }
        else {
            try {
                mGattService = mBTGatt.getService(ServiceUUID);
            }
            catch (NullPointerException e){
                Log.d("ADebugTag", "couldnt get service, mGattChar is null\n" + e);
            }
            try {
                mGattChar = mGattService.getCharacteristic(CharUUID);
            }
            catch (NullPointerException e){
                Log.d("ADebugTag", "couldnt get characteristic, mGattChar is null\n" + e);
            }
            return true;
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                mActivity.updatesStatus(MainActivity.STATE_CONNECTED,"");
                Log.d("ADebugTag", "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.d("ADebugTag", "Attempting to start service discovery:" + mBTGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                if (mBTAdapter.isEnabled()){
                    Log.d("ADebugTag", "enabled BT.");
                }
                else {
                    Log.d("ADebugTag", "didn't enable BT.");
                }
                mActivity.updatesStatus(MainActivity.STATE_DISCONNECTED,"");
                Log.d("ADebugTag", "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                if (mBTGatt ==null){
                    return;
                }
                else {
                    List<BluetoothGattService> services = getSupportedGattServices();
                    for (BluetoothGattService service : services) {
                        Log.d("ADebugTag", "services: "+service.getUuid());
                        if (!service.getUuid().equals(ServiceUUID))
                            continue;

                        List<BluetoothGattCharacteristic> gattCharacteristics =
                                service.getCharacteristics();

                        // Loops through available Characteristics.
                        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                            if (!gattCharacteristic.getUuid().equals(CharUUID))
                                continue;

                            final int charaProp = gattCharacteristic.getProperties();

                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                setCharacteristicNotification(gattCharacteristic, true);
                            } else {
                                Log.d("ADebugTag", "Characteristic does not support notify");
                            }
                        }
                    }
                }
            } else {
                Log.d("ADebugTag", "onServicesDiscovered received:" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                final byte[] dataInput = characteristic.getValue();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // For all other profiles, writes the data formatted in HEX.
        if (characteristic == null) {
            Log.d("ADebugTag", "characteristic is null in broadcastUpdate");
        }
        else {
            final byte[] data = characteristic.getValue();
            //Log.d("ADebugTag", "Data broadcast update: " + data);
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%X ", byteChar));
                String ds = stringBuilder.toString();
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + ds);
                Log.d("ADebugTag", "Data complete string: " + ds);
                mActivity.updatesStatus(MainActivity.DATA_RECEIVED,ds);
            }
            mContext.sendBroadcast(intent);
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBTAdapter == null || mBTGatt == null) {
            Log.d("ADebugTag", "BluetoothAdapter not initialized in set char");
            return;
        }
        else {
            mBTGatt.setCharacteristicNotification(characteristic, enabled);
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBTAdapter == null || mBTGatt == null) {
            Log.d("ADebugTag", "BluetoothAdapter not initialized");
            return;
        }
        mBTGatt.disconnect();
    }
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBTGatt == null) return null;

        return mBTGatt.getServices();
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBTGatt == null) {
            return;
        }
        mBTGatt.close();
        mBTGatt = null;
    }

}