package vendetta.blecar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class RemoteActivity extends Activity {

    Button connectButton;
    TextView connectTv;
    JoystickView joystickSpeed, joystickSteering;



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

        // Seek bar for controlling steering angle
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

        // Joystick for controlling speed
        joystickSpeed = findViewById(R.id.joystick_speed);
        joystickSpeed.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mBluetoothLEController.setSpeed(angle,strength);
                if (angle == strength)
                    Log.d(TAG,"GOT A RESET");
            }


        },200); // 5 times per second
        joystickSpeed.setEnabled(false);


        // Joystick for controlling steering
        joystickSteering = findViewById(R.id.joystick_steering);
        joystickSteering.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mBluetoothLEController.setSteering(angle,strength);
                if (angle == strength)
                    Log.d(TAG,"GOT A RESET");
            }


        },200); // 5 times per second
        joystickSteering.setEnabled(false);



        // initialize BLE controller with this activity
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
        joystickSpeed.setEnabled(true);
        joystickSteering.setEnabled(true);
        connectTv.setText("Connected");
        Toast.makeText(this, "Connected to " + getResources().getString(R.string.BLECarName), Toast.LENGTH_LONG).show();
    }

    public void setConnectionInactive(){
        connectButton.setClickable(true);
        joystickSpeed.setEnabled(false);
        joystickSteering.setEnabled(false);
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
