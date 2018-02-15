package vendetta.blecar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class RemoteActivity extends Activity {

    private Button connectButton;
    private TextView connectTv, maxSpeedTv,crtSpeedTV,crtSpeedValTV,crtSteeringTV,crtSteeringValTV;
    private JoystickView joystickSpeed, joystickSteering;
    private ProgressBar pbConnect;

    private boolean isConnectionActive = false;
    private final static String TAG = RemoteActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int JOYSTICK_UPDATE_INTERVAL = 200; // every 200 ms = 5 times per second

    private BluetoothLEController mBluetoothLEController;
    private boolean isConnectAllowed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);


        connectButton = findViewById(R.id.btn_connect);
        connectTv = findViewById(R.id.tv_conn_stat);
        maxSpeedTv = findViewById(R.id.tv_maxSpeed);
        pbConnect = findViewById(R.id.pb_connect);
        crtSpeedTV = findViewById(R.id.tv_crtSpeed);
        crtSpeedValTV = findViewById(R.id.tv_crtSpeedVal);
        crtSteeringTV = findViewById(R.id.tv_crtSteering);
        crtSteeringValTV = findViewById(R.id.tv_crtSteeringVal);

        // Seek bar for controlling steering angle
        SeekBar simpleSeekBar = findViewById(R.id.seek_maxSpeed);
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateMaxSpeed(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Switch for selecting between 1 or 2 joysticks for controlling speed & steering
        Switch joySelectSw = findViewById(R.id.sw_Joysticks);
        joySelectSw.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // 2 joysticks
                    joystickSpeed.setEnabled(isConnectionActive);
                    joystickSpeed.setButtonDirection(1); // vertical direction
                    joystickSpeed.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
                    joystickSpeed.setOnMoveListener(new JoystickView.OnMoveListener() {
                        @Override
                        public void onMove(int angle, int strength) {
                            mBluetoothLEController.setSpeed(angle, strength);
                        }

                    }, JOYSTICK_UPDATE_INTERVAL);

                    joystickSteering.setEnabled(isConnectionActive);
                    joystickSteering.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
                    joystickSteering.setOnMoveListener(new JoystickView.OnMoveListener() {
                        @Override
                        public void onMove(int angle, int strength) {
                            mBluetoothLEController.setSteeringByStrength(angle, strength);
                        }

                    }, JOYSTICK_UPDATE_INTERVAL);

                } else {
                    // 1 joystick
                    joystickSpeed.setEnabled(isConnectionActive);
                    joystickSpeed.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
                    joystickSpeed.setButtonDirection(0); // both directions
                    joystickSpeed.setOnMoveListener(new JoystickView.OnMoveListener() {
                        @Override
                        public void onMove(int angle, int strength) {
                            mBluetoothLEController.setSpeed(angle, strength);
                            mBluetoothLEController.setSteeringByAngle(angle, strength);
                        }

                    }, JOYSTICK_UPDATE_INTERVAL);


                    // Disable steering joystick
                    joystickSteering.setEnabled(false);
                    joystickSteering.setVisibility(View.INVISIBLE);
                }
            }
        });

        // TODO: check here if one or 2 joysticks need to be instantiated

        // Joystick for controlling speed
        joystickSpeed = findViewById(R.id.joystick_speed);
        joystickSpeed.setEnabled(false);
        joystickSpeed.setVisibility(View.INVISIBLE);

        joystickSpeed.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mBluetoothLEController.setSpeed(angle, strength);
            }

        }, JOYSTICK_UPDATE_INTERVAL);


        // Joystick for controlling steering
        joystickSteering = findViewById(R.id.joystick_steering);
        joystickSteering.setEnabled(false);
        joystickSteering.setVisibility(View.INVISIBLE);

        joystickSteering.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mBluetoothLEController.setSteeringByStrength(angle, strength);
            }

        }, JOYSTICK_UPDATE_INTERVAL);


        // initialize BLE controller with this activity
        mBluetoothLEController = new BluetoothLEController(this);

        this.checkForPermissions();
    }



    private void checkForPermissions() {
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

    public void setConnectionActive() {
        isConnectionActive = true;
        connectButton.setClickable(false);
        connectButton.setVisibility(View.INVISIBLE);
        joystickSpeed.setEnabled(true);
        joystickSteering.setEnabled(true);
        connectTv.setText("Connected");
        connectTv.setTextColor(Color.GREEN);
        joystickSpeed.setVisibility(View.VISIBLE);
        joystickSteering.setVisibility(View.VISIBLE);
        pbConnect.setVisibility(View.INVISIBLE);
        crtSpeedTV.setVisibility(View.VISIBLE);
        crtSpeedValTV.setVisibility(View.VISIBLE);
        crtSteeringTV.setVisibility(View.VISIBLE);
        crtSteeringValTV.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Connected to " + getResources().getString(R.string.PiCarMAC), Toast.LENGTH_LONG).show();
    }

    public void setConnectionInactive() {
        isConnectionActive = false;
        connectButton.setClickable(true);
        connectButton.setVisibility(View.VISIBLE);
        joystickSpeed.setEnabled(false);
        joystickSteering.setEnabled(false);
        connectTv.setText("Disconnected");
        connectTv.setTextColor(Color.RED);
        joystickSpeed.setVisibility(View.INVISIBLE);
        joystickSteering.setVisibility(View.INVISIBLE);
        crtSpeedTV.setVisibility(View.INVISIBLE);
        crtSpeedValTV.setVisibility(View.INVISIBLE);
        crtSteeringTV.setVisibility(View.INVISIBLE);
        crtSteeringValTV.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Lost connection to " + getResources().getString(R.string.PiCarMAC), Toast.LENGTH_LONG).show();
    }

    private void updateMaxSpeed(int value) {
        maxSpeedTv.setText(String.format(Locale.ENGLISH, "%d%%", value));
        mBluetoothLEController.setMaxSpeed(value);
    }

    public void postScanFailed(){
        Toast.makeText(this, "Could not find " + getResources().getString(R.string.PiCarMAC), Toast.LENGTH_LONG).show();
        pbConnect.setVisibility(View.INVISIBLE);
    }

    public void updateCrtSpeedTV(int value){
        this.crtSpeedValTV.setText(value+"%");
    }

    public void updateCrtSteeringTV(int value){
        this.crtSteeringValTV.setText(value+"%");
    }


    public void onConnectBtnPress(View v) {
        Log.d(TAG, "Attempting connection");
        if (isConnectAllowed) {
            pbConnect.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                public void run() {
                    mBluetoothLEController.attemptConnection();
                }
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
        mBluetoothLEController.close();
        finish();
    }

    @Override
    public void onStop(){
        mBluetoothLEController.close();
        super.onStop();
    }

    @Override
    public void onRestart(){
        Toast.makeText(this, "Bluetooth disconnected during sleep: Please reconnect", Toast.LENGTH_LONG).show();
        isConnectionActive = false;
        mBluetoothLEController = new BluetoothLEController(this);
        super.onRestart();
    }
}
