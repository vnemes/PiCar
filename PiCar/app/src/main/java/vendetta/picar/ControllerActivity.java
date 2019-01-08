package vendetta.picar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.Locale;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import vendetta.picar.camera.VideoFragment;
import vendetta.picar.camera.dependencies.Camera;
import vendetta.picar.camera.dependencies.Source;
import vendetta.picar.connection.ConnectionConfig;
import vendetta.picar.connection.ConnectionTypeEn;
import vendetta.picar.control.SpeedController;
import vendetta.picar.http.control.SpeedControllerHTTP;
import vendetta.picar.control.SteeringController;
import vendetta.picar.connection.PiWiFiManager;
import vendetta.picar.connection.ConnectionStateEn;
import vendetta.picar.http.control.SteeringControllerHTTP;
import vendetta.picar.http.requests.HealthCheckCyclicRequest;
import vendetta.picar.http.requests.CommandEnum;
import vendetta.picar.http.requests.ServiceEnum;
import vendetta.picar.http.requests.ServiceRequest;
import vendetta.picar.http.sensors.GPSSensor;
import vendetta.picar.http.sensors.UltrasonicSensor;

public class ControllerActivity extends Activity {

    // UI elements
    private TextView connectTv, maxSpeedTv, maxTV, crtSpeedTV, crtSpeedValTV, crtSteeringTV, crtSteeringValTV, distanceTV;
    private JoystickView joystickSpeed, joystickSteering;
    private ProgressBar pbConnect;
    private FrameLayout frameLayout;
    private VideoFragment videoFragment;

    // Sensors
    private SpeedController speedController;
    private UltrasonicSensor ultrasonicSensor;
    private GPSSensor gpsSensor;

    private boolean isConnectionActive = false;
    private boolean oneOrTwoJoysticks;
    private final static String TAG = ControllerActivity.class.getSimpleName();
    private static final int JOYSTICK_UPDATE_INTERVAL = 333; // every 333 ms = 3 times per second
    private HandlerThread ultrasonicHandlerThread;
    private Handler healthCheckHandler, wifiAPConnectionHandler;
    private ConnectionConfig config;
    private String effectiveIP = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        connectTv = findViewById(R.id.tv_conn_stat);
        maxSpeedTv = findViewById(R.id.tv_maxSpeed);
        maxTV = findViewById(R.id.tv_max);
        pbConnect = findViewById(R.id.pb_connect);
        crtSpeedTV = findViewById(R.id.tv_crtSpeed);
        crtSpeedValTV = findViewById(R.id.tv_crtSpeedVal);
        crtSteeringTV = findViewById(R.id.tv_crtSteering);
        crtSteeringValTV = findViewById(R.id.tv_crtSteeringVal);
        distanceTV = findViewById(R.id.tv_Distance);
        frameLayout = findViewById(R.id.video_frame);

        // Joystick for controlling speed
        joystickSpeed = findViewById(R.id.joystick_speed);

        // Joystick for controlling steering
        joystickSteering = findViewById(R.id.joystick_steering);

        config = new Gson().fromJson(getIntent().getStringExtra(getResources().getString(R.string.selected_connection_json_key)), ConnectionConfig.class);
        Log.d(TAG, "Attempting connection to " + config);

        onConnectionChange(ConnectionStateEn.CONNECTING);
        switch (config.getConnType()) {
            case WIFI_AP:
                //start monitoring wifi connection changes
                registerReceiver(PiWiFiManager.getReceiver(), PiWiFiManager.getFilter());
                wifiAPConnectionHandler = new Handler();
                //start 5 second connection timeout handler
                wifiAPConnectionHandler.postDelayed(() -> onConnectionChange(ConnectionStateEn.DISCONNECTED), 5000);
                PiWiFiManager.connectToWiFiAP(getApplicationContext(), config.getIdentifier(), config.getSecretValue());
                break;
            case WIFI_INET: //fall through
            case WIFI_LOCAL:
                establishConnection(config.getIdentifier());
                break;
            case MQTT:
                break;
            case BLE:
                break;
            default:
                break;
        }

    }

    public void establishConnection(String ip) {
        if (ip.startsWith("http://"))
            effectiveIP = ip;
        else effectiveIP = "http://" + ip;

        healthCheckHandler = new Handler();
        new ServiceRequest(this, effectiveIP)
                .requestForCallback(ServiceEnum.CONTROLLER_SERVICE, CommandEnum.START,
                        response -> {
                            onConnectionChange(ConnectionStateEn.CONNECTED);
                            new HealthCheckCyclicRequest(this, effectiveIP).connect(healthCheckHandler);
                        },
                        error -> {
                            Toast.makeText(this, "Cannot connect to " + effectiveIP, Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(() -> onConnectionChange(ConnectionStateEn.DISCONNECTED), 2000);
                        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        oneOrTwoJoysticks = preferences.getBoolean(this.getString(R.string.pref_key_joystick), false);
        int maxSpeed = preferences.getInt(this.getString(R.string.pref_key_speed_seek), 50);
        boolean enableCamera = preferences.getBoolean(this.getString(R.string.pref_key_enable_camera), false);
        boolean enableUltrasonic = preferences.getBoolean(this.getString(R.string.pref_key_enable_ultrasonic), false);

        //todo create gps settings entry
        gpsSensor = new GPSSensor(this);
        gpsSensor.setIp(effectiveIP);

        enableDisableUltrasonic(enableUltrasonic);
        enableControls(oneOrTwoJoysticks);
        updateMaxSpeed(maxSpeed);
        enableDisableCamera(enableCamera);
    }


    private void enableControls(boolean oneJoystickOnly) {
        SteeringController steeringController;

        if (config.getConnType().equals(ConnectionTypeEn.WIFI_AP) || config.getConnType().equals(ConnectionTypeEn.WIFI_INET) || config.getConnType().equals(ConnectionTypeEn.WIFI_LOCAL)) {
            speedController = new SpeedControllerHTTP(this, effectiveIP);
            steeringController = new SteeringControllerHTTP(this, effectiveIP);
        } else {
            // todo handle here BLE
            steeringController = new SteeringControllerHTTP(this,effectiveIP);
        }


        if (oneJoystickOnly) {
            // 1 joystick
            // Disable steering joystick
            joystickSteering.setEnabled(false);
            joystickSteering.setVisibility(View.INVISIBLE);

            joystickSpeed.setEnabled(isConnectionActive);
            joystickSpeed.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSpeed.setButtonDirection(0); // both directions
            joystickSpeed.setOnMoveListener(speedController::setSpeedStrAngle, JOYSTICK_UPDATE_INTERVAL);
            joystickSpeed.setOnMoveListener(steeringController::setSteeringAnglStr, JOYSTICK_UPDATE_INTERVAL);

        } else {
            // 2 joysticks
            joystickSpeed.setEnabled(isConnectionActive);
            joystickSpeed.setButtonDirection(1); // vertical direction
            joystickSpeed.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSpeed.setOnMoveListener(speedController::setSpeedStrAngle, JOYSTICK_UPDATE_INTERVAL);
            joystickSteering.setEnabled(isConnectionActive);
            joystickSteering.setVisibility(isConnectionActive ? View.VISIBLE : View.INVISIBLE);
            joystickSteering.setOnMoveListener(steeringController::setSteeringAnglStr, JOYSTICK_UPDATE_INTERVAL);
        }
    }

    private void enableDisableUltrasonic(boolean b) {
        if (b) {
            // send a request to start the ultrasonic sensor service
            ultrasonicSensor = new UltrasonicSensor(this);
            ultrasonicSensor.setIp(effectiveIP);
            new ServiceRequest(this, effectiveIP).request(ServiceEnum.ULTRASONIC_SERVICE, CommandEnum.RESTART);
            ultrasonicHandlerThread = new HandlerThread("HandlerThread");
            ultrasonicHandlerThread.start();
            Handler handler = new Handler(ultrasonicHandlerThread.getLooper());
            loading(true);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ultrasonicSensor.requestData();
                    handler.postDelayed(this, 1000);
                    loading(false);
                }
            }, 1000);
            distanceTV.setVisibility(View.VISIBLE);
        } else {
            if (ultrasonicHandlerThread != null) {
                ultrasonicHandlerThread.quit();
                //stop the ultrasonic sensor service
                new ServiceRequest(this, effectiveIP).request(ServiceEnum.ULTRASONIC_SERVICE, CommandEnum.STOP);
            }
            distanceTV.setText(R.string.ultrasonic_initializing);
            distanceTV.setVisibility(View.INVISIBLE);
        }
    }

    private void enableDisableCamera(boolean enableCamera) {
        if (enableCamera) {
            new ServiceRequest(this, effectiveIP).request(ServiceEnum.PICAMERA_SERVICE, CommandEnum.RESTART);
            //delay start of camera by 1 second to make sure the service was fully enabled
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Camera camera = new Camera(Source.ConnectionType.RawTcpIp, "picamera", effectiveIP.substring(7), 1324);
                    Log.d(getClass().getSimpleName(), "camera: " + camera.toString());

                    // get the frame layout
                    frameLayout.setVisibility(View.VISIBLE);

                    // create the video fragment
                    videoFragment = VideoFragment.newInstance(camera);
                    FragmentTransaction fragTran = getFragmentManager().beginTransaction();
                    fragTran.add(R.id.video_frame, videoFragment);
                    fragTran.commit();
                }
            }, 1000);

        } else {
            // send a request to stop the picamera service
            new ServiceRequest(this, effectiveIP).request(ServiceEnum.PICAMERA_SERVICE, CommandEnum.STOP);
            if (videoFragment != null) {
                videoFragment.stop();
                videoFragment = null;
                frameLayout.setVisibility(View.INVISIBLE);
            }
        }
    }


    public void onConnectionChange(ConnectionStateEn connectionState) {
        switch (connectionState) {
            case CONNECTING:
                loading(true);
                connectTv.setText(R.string.connection_connecting);
                connectTv.setTextColor(Color.YELLOW);
                joystickSpeed.setEnabled(false);
                joystickSteering.setEnabled(false);
                joystickSpeed.setVisibility(View.INVISIBLE);
                joystickSteering.setVisibility(View.INVISIBLE);
                crtSpeedTV.setVisibility(View.INVISIBLE);
                crtSpeedValTV.setVisibility(View.INVISIBLE);
                crtSteeringTV.setVisibility(View.INVISIBLE);
                crtSteeringValTV.setVisibility(View.INVISIBLE);
                distanceTV.setVisibility(View.INVISIBLE);
                maxSpeedTv.setVisibility(View.INVISIBLE);
                maxTV.setVisibility(View.INVISIBLE);
                break;

            case CONNECTED:
                isConnectionActive = true;
                enableControls(oneOrTwoJoysticks);
                connectTv.setText(R.string.connection_connected);
                connectTv.setTextColor(Color.GREEN);
                loading(false);
                crtSpeedTV.setVisibility(View.VISIBLE);
                crtSpeedValTV.setVisibility(View.VISIBLE);
                crtSteeringTV.setVisibility(View.VISIBLE);
                crtSteeringValTV.setVisibility(View.VISIBLE);
                maxSpeedTv.setVisibility(View.VISIBLE);
                maxTV.setVisibility(View.VISIBLE);

                Toast.makeText(this, "Connected to " + config.getIdentifier(), Toast.LENGTH_SHORT).show();

                break;
            case DISCONNECTED:
                isConnectionActive = false;
                loading(false);
                connectTv.setText(R.string.connection_disconnected);
                connectTv.setTextColor(Color.RED);
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
                // exit the current screen
                super.onBackPressed();
                break;
        }
    }


    private void updateMaxSpeed(int value) {
        maxSpeedTv.setText(String.format(Locale.ENGLISH, "%d%%", value));
        speedController.setMaxSpeed(value);
    }

    public void updateCrtSpeedTV(int value) {
        this.crtSpeedValTV.setText(String.format(Locale.ENGLISH, "%d%%", value));
    }

    public void updateCrtSteeringTV(int value) {
        this.crtSteeringValTV.setText(String.format(Locale.ENGLISH, "%d%%", value));
    }

    public void updateDistanceTV(double value) {
        this.distanceTV.setText(String.format(Locale.ENGLISH, "%s cm", value));
    }

    public void onLocateBtnPress(View v) { // todo move this into settings
        loading(true);
        new ServiceRequest(this, effectiveIP).request(ServiceEnum.GPS_SERVICE, CommandEnum.RESTART);
        new Handler().postDelayed(() -> gpsSensor.requestData(), 10000);

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
        new ServiceRequest(this, effectiveIP).request(ServiceEnum.GPS_SERVICE, CommandEnum.STOP);
    }

    public void loading(boolean isLoading) {
        pbConnect.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
    }

    public void onSettingsBtnPress(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void cancelWifiApTimout() {
        wifiAPConnectionHandler.removeCallbacksAndMessages(null);
    }

    public ConnectionConfig getConfig() {
        return config;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Back to main menu");
        builder.setMessage("Do you want to return to the main menu?");
        builder.setPositiveButton("Confirm", (dialog, id) -> super.onBackPressed());
        builder.setNegativeButton("Cancel", (dialog, id) -> {
        });
        builder.show();
    }

    @Override
    public void onDestroy() {
        if (ultrasonicHandlerThread != null)
            ultrasonicHandlerThread.quit();
        if (healthCheckHandler != null)
            healthCheckHandler.removeCallbacksAndMessages(null);


        if (isConnectionActive) { // disable all services enabled during runtime of the app
            new ServiceRequest(this, effectiveIP).request(ServiceEnum.PICAMERA_SERVICE, CommandEnum.STOP);
            new ServiceRequest(this, effectiveIP).request(ServiceEnum.ULTRASONIC_SERVICE, CommandEnum.STOP);
            new ServiceRequest(this, effectiveIP).request(ServiceEnum.CONTROLLER_SERVICE, CommandEnum.STOP);
            new ServiceRequest(this, effectiveIP).request(ServiceEnum.GPS_SERVICE, CommandEnum.STOP);
        }
        // unregister wifi connection receiver in order not to leak it
        if (config.getConnType().equals(ConnectionTypeEn.WIFI_AP)) {
            wifiAPConnectionHandler.removeCallbacksAndMessages(null);
            unregisterReceiver(PiWiFiManager.getReceiver());
        }
        super.onDestroy();
    }
}




