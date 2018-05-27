package vendetta.blecar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import vendetta.blecar.camera.VideoFragment;
import vendetta.blecar.camera.dependencies.Camera;
import vendetta.blecar.camera.dependencies.Source;
import vendetta.blecar.controllers.SpeedController;
import vendetta.blecar.controllers.SteeringController;
import vendetta.blecar.http.PiWiFiManager;
import vendetta.blecar.http.WiFiStateEnum;
import vendetta.blecar.requests.CheckConnectionRequest;
import vendetta.blecar.requests.CommandEnum;
import vendetta.blecar.requests.ServiceEnum;
import vendetta.blecar.requests.ServiceRequest;
import vendetta.blecar.sensors.GPSSensor;
import vendetta.blecar.sensors.UltrasonicSensor;

public class ControllerActivity extends Activity {

    // UI elements
    private Button connectButton, gpsButton;
    private TextView connectTv, maxSpeedTv, maxTV, crtSpeedTV, crtSpeedValTV, crtSteeringTV, crtSteeringValTV, distanceTV;
    private EditText ipInputET;
    private JoystickView joystickSpeed, joystickSteering;
    private ProgressBar pbConnect;
    private Switch joySelectSw, cameraOnOffSw, connectSw, ultrasonicSw;
    private FrameLayout frameLayout;
    private VideoFragment videoFragment;
    private SeekBar maxSpeedSeekBar;

    // Sensors
    private SpeedController speedController;
    private SteeringController steeringController;
    private UltrasonicSensor ultrasonicSensor;
    private GPSSensor gpsSensor;

    private boolean isConnectionActive = false;
    private final static String TAG = ControllerActivity.class.getSimpleName();
    private static final int JOYSTICK_UPDATE_INTERVAL = 333; // every 200 ms = 5 times per second
    private HandlerThread handlerThread = null;
    public static String IP = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);


        connectButton = findViewById(R.id.btn_connect);
        gpsButton = findViewById(R.id.btn_locate);
        connectTv = findViewById(R.id.tv_conn_stat);
        maxSpeedTv = findViewById(R.id.tv_maxSpeed);
        maxTV = findViewById(R.id.tv_max);
        pbConnect = findViewById(R.id.pb_connect);
        crtSpeedTV = findViewById(R.id.tv_crtSpeed);
        crtSpeedValTV = findViewById(R.id.tv_crtSpeedVal);
        crtSteeringTV = findViewById(R.id.tv_crtSteering);
        crtSteeringValTV = findViewById(R.id.tv_crtSteeringVal);
        distanceTV = findViewById(R.id.tv_Distance);
        ipInputET = findViewById(R.id.et_IP);
        maxSpeedSeekBar = findViewById(R.id.seek_maxSpeed);

        // Seek bar for controlling maximum speed
        maxSpeedSeekBar = findViewById(R.id.seek_maxSpeed);
        maxSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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

        // Switch for enabling/disabling camera overlay
        cameraOnOffSw = findViewById(R.id.sw_Camera);
        cameraOnOffSw.setOnCheckedChangeListener((compoundButton, b) -> enableDisableCamera(b));

        //Switch for enabling local connection
        connectSw = findViewById(R.id.sw_connect);
        connectSw.setOnCheckedChangeListener((compoundButton, b) -> ipInputET.setVisibility(b ? View.INVISIBLE : View.VISIBLE));

        //Switch for enabling distance measurement via ultrasonic sensor
        ultrasonicSw = findViewById(R.id.sw_ultrasonic);
        ultrasonicSw.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                // send a request to start the ultrasonic sensor service
                new ServiceRequest(this, IP).request(ServiceEnum.ULTRASONIC_SERVICE, CommandEnum.RESTART);
                handlerThread = new HandlerThread("HandlerThread");
                handlerThread.start();
                Handler handler = new Handler(handlerThread.getLooper());
                loading(true);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        ultrasonicSensor.requestData();
                        handler.postDelayed(this, 1000);
                        loading(false);
                    }
                };
                handler.postDelayed(runnable, 1000);
                distanceTV.setVisibility(View.VISIBLE);
            } else {
                handlerThread.quit();
                //stop the ultrasonic sensor service
                new ServiceRequest(this, IP).request(ServiceEnum.ULTRASONIC_SERVICE, CommandEnum.STOP);
                distanceTV.setText("Initializing..");
                distanceTV.setVisibility(View.INVISIBLE);
            }
        });

        // Joystick for controlling speed
        joystickSpeed = findViewById(R.id.joystick_speed);
        joystickSpeed.setEnabled(false);
        joystickSpeed.setVisibility(View.INVISIBLE);

        // Joystick for controlling steering
        joystickSteering = findViewById(R.id.joystick_steering);
        joystickSteering.setEnabled(false);
        joystickSteering.setVisibility(View.INVISIBLE);

        // Speed & Steering controllers, sensors
        speedController = new SpeedController(this);
        steeringController = new SteeringController(this);
        ultrasonicSensor = new UltrasonicSensor(this);
        gpsSensor = new GPSSensor(this);


        //start monitoring wifi connection changes
        registerReceiver(PiWiFiManager.getReceiver(), PiWiFiManager.getFilter());

    }

    private void enableControls(boolean areTwoJoysticksRequired) {
        if (areTwoJoysticksRequired) {
            // 2 joysticks
            joystickSpeed.setEnabled(isConnectionActive);
            joystickSpeed.setButtonDirection(1); // vertical direction
            joystickSpeed.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSpeed.setOnMoveListener(speedController::setSpeedStrAngle, JOYSTICK_UPDATE_INTERVAL);
            joystickSteering.setEnabled(isConnectionActive);
            joystickSteering.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSteering.setOnMoveListener(steeringController::setSteeringAnglStr, JOYSTICK_UPDATE_INTERVAL);
        } else {
            // 1 joystick

            // Disable steering joystick
            joystickSteering.setEnabled(false);
            joystickSteering.setVisibility(View.INVISIBLE);

            joystickSpeed.setEnabled(isConnectionActive);
            joystickSpeed.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSpeed.setButtonDirection(0); // both directions
            joystickSpeed.setOnMoveListener(speedController::setSpeedStrAngle, JOYSTICK_UPDATE_INTERVAL);
            joystickSpeed.setOnMoveListener(steeringController::setSteeringAnglStr, JOYSTICK_UPDATE_INTERVAL);
        }
    }

    private void enableDisableCamera(boolean enableCamera) {
        if (enableCamera) {
            new ServiceRequest(this, IP).request(ServiceEnum.PICAMERA_SERVICE, CommandEnum.RESTART);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Camera camera = new Camera(Source.ConnectionType.RawTcpIp, "picamera", IP, 1324);
                    Log.d(getClass().getSimpleName(), "camera: " + camera.toString());

                    // get the frame layout
                    frameLayout = findViewById(R.id.video_frame);
                    frameLayout.setVisibility(View.VISIBLE);

                    // create the video fragment
                    videoFragment = VideoFragment.newInstance(camera);
                    FragmentTransaction fragTran = getFragmentManager().beginTransaction();
                    fragTran.add(R.id.video_frame, videoFragment);
                    fragTran.commit();
                }
            }, 500);

        } else {
            // send a request to stop the picamera service
            new ServiceRequest(this, IP).request(ServiceEnum.PICAMERA_SERVICE, CommandEnum.STOP);
            videoFragment.stop();
            videoFragment = null;
            frameLayout.setVisibility(View.INVISIBLE);
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
                gpsButton.setClickable(true);
                gpsButton.setVisibility(View.VISIBLE);
                enableControls(joySelectSw.isChecked());
                connectTv.setText("Connected");
                connectTv.setTextColor(Color.GREEN);
                pbConnect.setVisibility(View.INVISIBLE);
                ipInputET.setVisibility(View.INVISIBLE);
                connectSw.setVisibility(View.INVISIBLE);
                crtSpeedTV.setVisibility(View.VISIBLE);
                crtSpeedValTV.setVisibility(View.VISIBLE);
                crtSteeringTV.setVisibility(View.VISIBLE);
                crtSteeringValTV.setVisibility(View.VISIBLE);
                maxSpeedTv.setVisibility(View.VISIBLE);
                maxTV.setVisibility(View.VISIBLE);
                joySelectSw.setVisibility(View.VISIBLE);
                cameraOnOffSw.setVisibility(View.VISIBLE);
                ultrasonicSw.setVisibility(View.VISIBLE);
                maxSpeedSeekBar.setVisibility(View.VISIBLE);
                speedController.setIp(IP);
                steeringController.setIp(IP);
                ultrasonicSensor.setIp(IP);
                gpsSensor.setIp(IP);
                //todo set ip of every sensor here

                new ServiceRequest(this, IP).request(ServiceEnum.CONTROLLER_SERVICE, CommandEnum.START);
                Toast.makeText(this, "Connected to " + getResources().getString(R.string.pi_wifi_ssid), Toast.LENGTH_SHORT).show();


                break;
            case DISCONNECTED:
                isConnectionActive = false;
                pbConnect.setVisibility(View.INVISIBLE);
                connectButton.setClickable(true);
                connectButton.setVisibility(View.VISIBLE);
                gpsButton.setClickable(false);
                gpsButton.setVisibility(View.INVISIBLE);
                ipInputET.setVisibility(connectSw.isChecked() ? View.INVISIBLE : View.VISIBLE);
                connectSw.setVisibility(View.VISIBLE);
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
                distanceTV.setVisibility(View.INVISIBLE);
                maxSpeedTv.setVisibility(View.INVISIBLE);
                maxTV.setVisibility(View.INVISIBLE);
                ultrasonicSw.setVisibility(View.INVISIBLE);
                joySelectSw.setVisibility(View.INVISIBLE);
                cameraOnOffSw.setVisibility(View.INVISIBLE);
                maxSpeedSeekBar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private void updateMaxSpeed(int value) {
        maxSpeedTv.setText(String.format(Locale.ENGLISH, "%d%%", value));
        speedController.setMaxSpeed(value);
    }

    public void updateCrtSpeedTV(int value) {
        this.crtSpeedValTV.setText(value + "%");
    }

    public void updateCrtSteeringTV(int value) {
        this.crtSteeringValTV.setText(value + "%");
    }

    public void updateDistanceTV(double value) {
        this.distanceTV.setText(value + " cm");
    }

    public static String getIP() {
        return IP;
    }


    public void onLocateBtnPress(View v) {
        loading(true);
        new ServiceRequest(this, IP).request(ServiceEnum.GPS_SERVICE, CommandEnum.RESTART);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gpsSensor.requestData();
            }
        }, 10000);

    }

    public void showMap(double lat, double lng) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogmap);
        dialog.show();

        MapView mMapView = dialog.findViewById(R.id.mapView);
        mMapView.onCreate(dialog.onSaveInstanceState());
        mMapView.onResume();// needed to get the map to display immediately
        mMapView.getMapAsync(googleMap -> {
            LatLng latLng = new LatLng(lat, lng);
            CameraPosition cameraPosition = new CameraPosition.Builder().
                    target(latLng).
                    tilt(60).
                    zoom(17.5f).
                    bearing(0).
                    build();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("PiCar"));
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });
        loading(false);
        new ServiceRequest(this, IP).request(ServiceEnum.GPS_SERVICE, CommandEnum.STOP);
    }

    public void loading(boolean isLoading) {
        pbConnect.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
    }

    public void onConnectBtnPress(View v) {
        onConnectionChange(WiFiStateEnum.CONNECTING);
        Log.d(TAG, "Attempting connection");
        if (connectSw.isChecked()) {
            IP = getApplicationContext().getString(R.string.pi_url);
            if (!PiWiFiManager.connectToWiFiAP(getApplicationContext()))
                Toast.makeText(this, "Cannot connect to " + getResources().getString(R.string.pi_wifi_ssid), Toast.LENGTH_SHORT).show();
        } else {
            IP = "http://" + ipInputET.getText().toString();
            new CheckConnectionRequest(this, IP).connect();
        }
    }


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm exit");
        builder.setMessage("Do you want to exit the application?");
        builder.setPositiveButton("Exit", (dialog, id) -> {
            super.onBackPressed();
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
        });
        builder.show();
    }

    @Override
    public void onDestroy() {
        if (handlerThread != null)
            handlerThread.quit();
        if (IP != null && isConnectionActive) { // disable all services enabled during runtime of the app
            new ServiceRequest(this, IP).request(ServiceEnum.PICAMERA_SERVICE, CommandEnum.STOP);
            new ServiceRequest(this, IP).request(ServiceEnum.ULTRASONIC_SERVICE, CommandEnum.STOP);
            new ServiceRequest(this, IP).request(ServiceEnum.CONTROLLER_SERVICE, CommandEnum.STOP);
            new ServiceRequest(this, IP).request(ServiceEnum.GPS_SERVICE, CommandEnum.STOP);
        }
        // unregister wifi connection receiver in order not to leak it
        unregisterReceiver(PiWiFiManager.getReceiver());
        finish();
        super.onDestroy();
    }
}




