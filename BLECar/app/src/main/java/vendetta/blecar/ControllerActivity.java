package vendetta.blecar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Locale;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class ControllerActivity extends Activity {

    private Button connectButton;
    private TextView connectTv, maxSpeedTv, crtSpeedTV, crtSpeedValTV, crtSteeringTV, crtSteeringValTV;
    private JoystickView joystickSpeed, joystickSteering;
    private ProgressBar pbConnect;
    Switch joySelectSw;

    private boolean isConnectionActive = false;
    private final static String TAG = ControllerActivity.class.getSimpleName();
    private static final int JOYSTICK_UPDATE_INTERVAL = 200; // every 200 ms = 5 times per second

    private int lastSpeedStr = 0;
    private int lastSpeedAngle = 0;
    private int lastSteerStr = 0;
    private int lastSteerAngl = 0;
    private int speedLimit = 50;


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

        // Seek bar for controlling maximum speed
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
        joySelectSw = findViewById(R.id.sw_Joysticks);
        joySelectSw.setOnCheckedChangeListener((compoundButton, b) -> enableControls(b));

        // Joystick for controlling speed
        joystickSpeed = findViewById(R.id.joystick_speed);
        joystickSpeed.setEnabled(false);
        joystickSpeed.setVisibility(View.INVISIBLE);

        // Joystick for controlling steering
        joystickSteering = findViewById(R.id.joystick_steering);
        joystickSteering.setEnabled(false);
        joystickSteering.setVisibility(View.INVISIBLE);


        //start monitoring wifi connection changes
        registerReceiver(PiWiFiManager.getReceiver(), PiWiFiManager.getFilter());
    }

    private void enableControls(boolean b) {
        if (b) {
            // 2 joysticks
            joystickSpeed.setEnabled(isConnectionActive);
            joystickSpeed.setButtonDirection(1); // vertical direction
            joystickSpeed.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSpeed.setOnMoveListener(this::setSpeed, JOYSTICK_UPDATE_INTERVAL);
            joystickSteering.setEnabled(isConnectionActive);
            joystickSteering.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSteering.setOnMoveListener(this::setSteering, JOYSTICK_UPDATE_INTERVAL);
        } else {
            // 1 joystick

            // Disable steering joystick
            joystickSteering.setEnabled(false);
            joystickSteering.setVisibility(View.INVISIBLE);

            joystickSpeed.setEnabled(isConnectionActive);
            joystickSpeed.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSpeed.setButtonDirection(0); // both directions
            joystickSpeed.setOnMoveListener(this::setSpeed, JOYSTICK_UPDATE_INTERVAL);
            joystickSpeed.setOnMoveListener(this::setSteering, JOYSTICK_UPDATE_INTERVAL);
        }
    }


    public void onConnectionChange(WiFiStateEnum connectionState) {
        switch (connectionState) {
            case CONNECTING:
                Toast.makeText(this, "Connecting..", Toast.LENGTH_SHORT).show();
                pbConnect.setVisibility(View.VISIBLE);
                connectButton.setClickable(false);
                connectTv.setText("Connecting");
                connectTv.setTextColor(Color.YELLOW);
                break;

            case CONNECTED:
                isConnectionActive = true;
                connectButton.setClickable(false);
                connectButton.setVisibility(View.INVISIBLE);
                enableControls(joySelectSw.isChecked());
                connectTv.setText("Connected");
                connectTv.setTextColor(Color.GREEN);
                pbConnect.setVisibility(View.INVISIBLE);
                crtSpeedTV.setVisibility(View.VISIBLE);
                crtSpeedValTV.setVisibility(View.VISIBLE);
                crtSteeringTV.setVisibility(View.VISIBLE);
                crtSteeringValTV.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Connected to " + getResources().getString(R.string.pi_wifi_ssid), Toast.LENGTH_SHORT).show();
                break;
            case DISCONNECTED:
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
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private void updateMaxSpeed(int value) {
        maxSpeedTv.setText(String.format(Locale.ENGLISH, "%d%%", value));
        speedLimit = value;
    }

    public void updateCrtSpeedTV(int value) {
        this.crtSpeedValTV.setText(value + "%");
    }

    public void updateCrtSteeringTV(int value) {
        this.crtSteeringValTV.setText(value + "%");
    }


    public void onConnectBtnPress(View v) {
        Log.d(TAG, "Attempting connection");

        if (!PiWiFiManager.connectToWiFiAP(getApplicationContext()))
            Toast.makeText(this, "Cannot connect to "+ getResources().getString(R.string.pi_wifi_ssid), Toast.LENGTH_SHORT).show();
    }


    public void setSpeed(int angle, int strength) {

        strength = strength < speedLimit ? strength : speedLimit;

        if (strength == lastSpeedStr && angle == lastSpeedAngle)
            return;
        lastSpeedStr = strength;
        lastSpeedAngle = angle;

        byte val = (byte) strength;
        byte dir = 0;
        if (Math.sin(Math.toRadians(angle)) >= 0) {
            dir = 1;
            updateCrtSpeedTV(strength);
        } else {
            updateCrtSpeedTV(-strength);
        }

        byte[] value = new byte[2];
        value[0] = dir;
        value[1] = val;


        Log.d(TAG, "wrote speed " + val);
    }

    public void setSteering(int angle, int strength) {

        if (strength == lastSteerStr && angle == lastSteerAngl)
            return;
        lastSteerStr = strength;
        lastSteerAngl = angle;

        byte val = (byte) strength;
        byte dir = 0;
        if (Math.sin(Math.toRadians(angle)) >= 0) {
            dir = 1;
            updateCrtSteeringTV(strength);
        } else {
            updateCrtSteeringTV(-strength);
        }

        byte[] value = new byte[2];
        value[0] = dir;
        value[1] = val;


        Log.d(TAG, "wrote steering " + val);
    }


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm exit");
        builder.setMessage("Do you want to exit the application?");
        builder.setPositiveButton("Exit", (dialog, id) -> finish());
        builder.setNegativeButton("Cancel", (dialog, id) -> {});
        
        builder.show();
    }
}




