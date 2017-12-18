package vendetta.blecar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;

public class RemoteActivity extends Activity {

    Button connectButton;
    TextView connectTv;



    private final static String TAG = RemoteActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BluetoothLEController mBluetoothLEController;
    private boolean isConnectAllowed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);



        connectButton = findViewById(R.id.btn_connect);
        connectTv = findViewById(R.id.tv_conn_stat);
        SeekBar simpleSeekBar = findViewById(R.id.seek_acceleration);

        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBluetoothLEController.setSteering(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) { }

            public void onStopTrackingTouch(SeekBar seekBar) {
                mBluetoothLEController.setSteering(seekBar.getProgress());
            }
        });


        mBluetoothLEController = new BluetoothLEController(this);
        requestPermissions();
    }

    private void requestPermissions() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        } else {
            isConnectAllowed = true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                    isConnectAllowed = true;
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to communicate with the Car.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void setConnectionActive(){
        connectButton.setClickable(false);
        connectTv.setText("Connected");
        Toast.makeText(this, "Connected to " + getResources().getString(R.string.BLECarName), Toast.LENGTH_LONG).show();
    }

    public void setConnectionInactive(){
        connectButton.setClickable(true);
        connectTv.setText("Disconnected");
        Toast.makeText(this, "Lost connection to the Car", Toast.LENGTH_LONG).show();
    }



    public void onConnectBtnPress(View v) {
        Log.d(TAG, "Attempting connection");
        if (isConnectAllowed)
            new Thread(new Runnable() {
                public void run() {
                    mBluetoothLEController.attemptConnection();
                }
            }).start();
    }

    @Override
    public void onBackPressed() {
        mBluetoothLEController.close();
        finish();
    }
}
