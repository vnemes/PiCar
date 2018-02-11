package vendetta.blecar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by Vendetta on 18-Dec-17.
 */

public class BluetoothLEController {

    private final static String TAG = RemoteActivity.class.getSimpleName();
    private RemoteActivity mActivity;

    private int maxSpeed;
    private final static int MAX_SPEED_INIT = 50; // 50% maximum speed

    private Queue<BluetoothGattCharacteristic> bleTxMessageQueue = new LinkedList<>();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private static final int REQUEST_ENABLE_BT = 1;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();

    private BluetoothGattCharacteristic steeringCharacteristic = null;
    private BluetoothGattCharacteristic speedCharacteristic = null;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    private final static UUID MOVEMENT_SERVICE_UUID =
            UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");

    private static final UUID STEERING_CHARACTERISTIC_UUID =
            UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    private static final UUID SPEED_CHARACTERISTIC_UUID =
            UUID.fromString("2eabb1e1-ae0f-4eb8-bfdc-f564ad55f359");


    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            scanLeDevice(false);
            mBluetoothGatt = result.getDevice().connectGatt(mActivity.getApplicationContext(), true, mGattCallback);
            Log.d(TAG, result.getDevice().toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Bluetooth scan failed!");
        }
    };

    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        mConnectionState = STATE_CONNECTED;
                        gatt.readRemoteRssi();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.setConnectionActive();
                            }
                        });
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.setConnectionInactive();
                            }
                        });
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Services received here!
                        Log.d(TAG, "Discovered services");
                        List<BluetoothGattService> mServices = gatt.getServices();
                        for (BluetoothGattService bgs : mServices) {
                            Log.d(TAG, bgs.getUuid().toString());
                            if (bgs.getUuid().equals(MOVEMENT_SERVICE_UUID)) {
                                List<BluetoothGattCharacteristic> mCharact = bgs.getCharacteristics();
                                Log.d(TAG, "Discovered characteristics");
                                for (BluetoothGattCharacteristic bgc : mCharact) {

                                    Log.d(TAG, bgc.getUuid().toString());
                                    if (bgc.getUuid().equals(STEERING_CHARACTERISTIC_UUID))
                                        steeringCharacteristic = bgc;
                                    else if (bgc.getUuid().equals(SPEED_CHARACTERISTIC_UUID))
                                        speedCharacteristic = bgc;

                                }
                            }
                        }
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Read action here with characteristic here!
                        Log.d(TAG, "Read Characteristic " + characteristic.toString());
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        /**
                         * debug:  Log.d(TAG, "Written Characteristic "+characteristic.getUuid().toString()+" with value: "+characteristic.getValue());
                         */

                        // remove command from queue only after the callback for successful write was triggered!
                        synchronized (bleTxMessageQueue) {
                            //bleTxMessageQueue.poll();
                            if (!bleTxMessageQueue.isEmpty()) {
                                mBluetoothGatt.writeCharacteristic(bleTxMessageQueue.poll());
                            }
                        }
                    }
                }

                public void onReadRemoteRssi(BluetoothGatt gatt,
                                             int rssi,
                                             int status) {
                    //TODO display dBm for active connections
                }
            };


    public BluetoothLEController(RemoteActivity mActivity) {
        this.mActivity = mActivity;
        maxSpeed = MAX_SPEED_INIT;
    }

    public void setSteeringByStrength(int angle, int strength) {
        int stVal = strength != 0 ? (int) (strength * Math.cos(Math.toRadians(angle))) : 0;
        int amount = (127 * Math.abs(stVal)) / 100;
        byte val = (byte) amount;
        if (stVal < 0) {
            val = (byte) (val & (byte) 0x7F);
        } else {
            val = (byte) (val | (byte) 0x80);
        }
        mActivity.updateCrtSteeringTV(stVal);
        byte[] value = new byte[1];
        value[0] = val;
        steeringCharacteristic.setValue(value);
        if (bleTxMessageQueue.isEmpty())
            mBluetoothGatt.writeCharacteristic(steeringCharacteristic);
        bleTxMessageQueue.add(steeringCharacteristic);
    }


    public void setSpeed(int angle, int strength) {
        /**
         * [0, 100] -> [0, 127]
         * m = 127 / 100
         * y = m * (x - 0 ) + 0
         */
        strength = strength < maxSpeed ? strength : maxSpeed;
        int amount = (127 * strength) / 100;
        byte val = (byte) amount;
        if (Math.sin(Math.toRadians(angle)) >= 0) {
            val = (byte) (val | (byte) 0x80);
            mActivity.updateCrtSpeedTV(strength);
        } else {
            val = (byte) (val & (byte) 0x7F);
            mActivity.updateCrtSpeedTV(-strength);
        }

        byte[] value = new byte[1];
        value[0] = val;
        speedCharacteristic.setValue(value);
        if (bleTxMessageQueue.isEmpty())
            mBluetoothGatt.writeCharacteristic(speedCharacteristic);
        bleTxMessageQueue.add(speedCharacteristic);
    }

    public void setSteeringByAngle(int angle, int strength) {
        /**
         * [0, 100] -> [0, 127]
         * m = 127 / 100
         * y = m * (x - 0 ) + 0
         */

        int stVal = strength != 0 ? (int) (100 * Math.cos(Math.toRadians(angle))) : 0;
        int amount = (127 * Math.abs(stVal)) / 100;
        byte val = (byte) amount;
        if (stVal < 0) {
            val = (byte) (val & (byte) 0x7F);
        } else {
            val = (byte) (val | (byte) 0x80);
        }
        mActivity.updateCrtSteeringTV(stVal);
//        int amount = (127 * strength) / 100;
//        byte val = (byte) amount;
//        if (Math.sin((double) angle) >= 0) {
//            val = (byte) (val | (byte) 0x80);
//            mActivity.updateCrtSteeringTV(strength);
//        } else {
//            val = (byte) (val & (byte) 0x7F);
//            mActivity.updateCrtSteeringTV(-strength);
//        }

        byte[] value = new byte[1];
        value[0] = val;
        steeringCharacteristic.setValue(value);
        if (bleTxMessageQueue.isEmpty())
            mBluetoothGatt.writeCharacteristic(steeringCharacteristic);
        bleTxMessageQueue.add(steeringCharacteristic);
    }


    public void attemptConnection() {

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scanLeDevice(true);
    }


    private void scanLeDevice(final boolean enable) {

        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        mActivity.postScanFailed();
                        mScanning = false;
                    }
                    bluetoothLeScanner.stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            ScanFilter.Builder mScanFilterBuilder = new ScanFilter.Builder();
            mScanFilterBuilder.setDeviceName(mActivity.getResources().getString(R.string.BLECarName));
            List<ScanFilter> scanFilterList = new ArrayList<>();
            scanFilterList.add(mScanFilterBuilder.build());
            bluetoothLeScanner.startScan(scanFilterList, new ScanSettings.Builder().build(), mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    public void setMaxSpeed(int value) {
        this.maxSpeed = value;
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}
