package vendetta.blecar;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;
import java.util.Locale;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import com.trello.rxlifecycle.components.RxActivity;

import static com.trello.rxlifecycle.android.ActivityEvent.PAUSE;

public class RemoteActivity extends RxActivity {

    private Button connectButton;
    private TextView connectTv, maxSpeedTv, crtSpeedTV, crtSpeedValTV, crtSteeringTV, crtSteeringValTV;
    private JoystickView joystickSpeed, joystickSteering;
    private ProgressBar pbConnect;
    Switch joySelectSw;

    private boolean isConnectionActive = false;
    private final static String TAG = RemoteActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int JOYSTICK_UPDATE_INTERVAL = 200; // every 200 ms = 5 times per second

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    private RxBleDevice bleDevice;
    private Subscription stateSubscription;
    private Observable<RxBleConnection> connectionObservable;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();

    private BluetoothAdapter bluetoothAdapter;
    private RxBleClient rxBleClient = null;
    private boolean isConnectAllowed = false;

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

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return bleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(bindUntilEvent(PAUSE))
                .compose(new ConnectionSharingAdapter());
    }


    private void checkForPermissions() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION));
            builder.show();
        } else {
            isConnectAllowed = true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
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
                    builder.setOnDismissListener(dialog -> {
                    });
                    builder.show();
                }
            }
        }
    }

    public void onConnectionChange(RxBleConnection.RxBleConnectionState connection) {
        switch (connection){
            case CONNECTING: {
                // TODO: Add timeout period while scanning for BLE Device
                Toast.makeText(this, "Connecting..", Toast.LENGTH_SHORT).show();
                pbConnect.setVisibility(View.VISIBLE);
                connectButton.setClickable(false);
                connectTv.setText("Connecting");
                connectTv.setTextColor(Color.YELLOW);
                 break;
            }
            case CONNECTED: {
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
                Toast.makeText(this, "Connected to " + getResources().getString(R.string.PiCarMAC), Toast.LENGTH_LONG).show();
                break;
            }
            case DISCONNECTED:{
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
    }

    private void updateMaxSpeed(int value) {
        maxSpeedTv.setText(String.format(Locale.ENGLISH, "%d%%", value));
        speedLimit = value;
    }

    public void postScanFailed() {
        Toast.makeText(this, "Could not find " + getResources().getString(R.string.PiCarMAC), Toast.LENGTH_LONG).show();
        pbConnect.setVisibility(View.INVISIBLE);
    }

    public void updateCrtSpeedTV(int value) {
        this.crtSpeedValTV.setText(value + "%");
    }

    public void updateCrtSteeringTV(int value) {
        this.crtSteeringValTV.setText(value + "%");
    }


    public void onConnectBtnPress(View v) {
        Log.d(TAG, "Attempting connection");
        if (isConnectAllowed) {
            connectionObservable
                    .unsubscribeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(this::clearConnectionObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( //TODO discover services after connecting
                            rxBleConnection -> rxBleConnection.discoverServices()
                                    .takeUntil(disconnectTriggerSubject)
                                    .compose(bindUntilEvent(PAUSE))
                                    .unsubscribeOn(AndroidSchedulers.mainThread())
                                    .subscribe(result ->Log.d(TAG, "Got services: " + result.getBluetoothGattServices().toString())),
                            throwable -> {
                                // Handle an error here.
                                Log.d(TAG, "Error on establishing connection: " + throwable.getMessage());
                            }
                    );
        }
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

        connectionObservable.flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString("2eabb1e1-ae0f-4eb8-bfdc-f564ad55f359"), value))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> Log.d(TAG, "Values written to the Pi: " + bytes[0]+" "+bytes[1]), throwable -> Log.d(TAG, "ERROR ON WRITE " + throwable.getMessage()));


        Log.d(TAG,"wrote speed "+val);
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

        connectionObservable.flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"), value))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> Log.d(TAG, "Values written to the Pi: " + bytes[0]+" "+bytes[1]), throwable -> Log.d(TAG, "ERROR ON WRITE " + throwable.getMessage()));


        Log.d(TAG,"wrote steering "+val);
    }




    @Override
    public void onBackPressed() {

        // TODO add prompt for the user to confirm exit
        finish();
    }

    void clearConnectionObservable() {

        // attempt at forcing the garbage collector to always release the observables
        // helps in the case of fast context switches between apps

        connectionObservable = null;
//        stateSubscription = null;
        Log.d(TAG, "UNSUBSCRIBED!");
    }

    void clearStatusSubscription(){
        stateSubscription = null;
        Log.d(TAG,"SUBSCRIPTION");
    }

    @Override
    public void onStop() {

        // when the app is put in background, un subscribe from the observables in order to save power
        disconnectTriggerSubject.onNext(null);
        onConnectionChange(RxBleConnection.RxBleConnectionState.DISCONNECTED);

        super.onStop();
    }

    @Override
    public void onRestart() {

        //prompt the user to reconnect after the app was no longer in foreground
        Toast.makeText(this, "Bluetooth disconnected during sleep: Please reconnect", Toast.LENGTH_LONG).show();

        super.onRestart();
    }


    @Override
    protected void onResume() {

        //re-check in case of permission changes between foreground sessions
        this.checkForPermissions();
        //re-attach connection observable and re-initialize the connection even after onPause/onStop
        rxBleClient = RxBleClientSingleton.getInstance(this);
        bleDevice = rxBleClient.getBleDevice(getResources().getString(R.string.PiCarMAC));
        stateSubscription = bleDevice.observeConnectionStateChanges()
                .takeUntil(disconnectTriggerSubject)
                .compose(bindUntilEvent(PAUSE))
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::clearStatusSubscription)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionChange, throwable -> Log.d(TAG,throwable.getMessage()));
        connectionObservable = prepareConnectionObservable();

        super.onResume();
    }
}
