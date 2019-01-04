package vendetta.blecar.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;

import vendetta.blecar.ControllerActivity;
import vendetta.blecar.R;
import vendetta.blecar.requests.CommandEnum;
import vendetta.blecar.requests.HealthCheckCyclicRequest;
import vendetta.blecar.requests.ServiceEnum;
import vendetta.blecar.requests.ServiceRequest;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Vendetta on 29-Apr-18.
 */

public class PiWiFiManager {

    private static NetworkInfo.State lastState = NetworkInfo.State.DISCONNECTED;
    private static BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ControllerActivity activity = (ControllerActivity) context;

            Bundle extras = intent.getExtras();
            if (extras != null) {
                NetworkInfo ni = (NetworkInfo) extras.get("networkInfo");
                switch (ni.getState()) {
                    case DISCONNECTED:
                        if (lastState != NetworkInfo.State.DISCONNECTED) {
                            activity.onConnectionChange(WiFiStateEnum.DISCONNECTED);
                            lastState = NetworkInfo.State.DISCONNECTED;
                        }
                        break;
                    case CONNECTED:
                        if (lastState != NetworkInfo.State.CONNECTED) {
                            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                            WifiInfo wifiInfo = wifi.getConnectionInfo();
                            if (wifiInfo.getSSID().replaceAll("^\"|\"$", "").equals(context.getString(R.string.pi_wifi_ssid))) {
                                new ServiceRequest(context, activity.IP)
                                        .requestForCallback(ServiceEnum.CONTROLLER_SERVICE, CommandEnum.START,
                                                response -> activity.onConnectionChange(WiFiStateEnum.CONNECTED),
                                                error -> {
                                                    Toast.makeText(context, "Cannot connect to " + activity.IP, Toast.LENGTH_SHORT).show();
                                                    new Handler().postDelayed(() -> activity.onConnectionChange(WiFiStateEnum.DISCONNECTED), 1000);
                                                });


                            } else
                                activity.onConnectionChange(WiFiStateEnum.DISCONNECTED);

                            lastState = NetworkInfo.State.CONNECTED;
                        }
                        break;
                    case CONNECTING:
                        if (lastState != NetworkInfo.State.CONNECTING) {
                            activity.onConnectionChange(WiFiStateEnum.CONNECTING);
                            lastState = NetworkInfo.State.CONNECTING;
                        }
                        break;
                }
            }
        }
    };

    public static BroadcastReceiver getReceiver() {
        return receiver;
    }

    public static IntentFilter getFilter() {
        return new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    public static boolean connectToWiFiAP(Context context, String ssid) {

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = String.format("\"%s\"", ssid);
        conf.preSharedKey = String.format("\"%s\"", context.getString(R.string.pi_wifi_pw)); // todo remove hardcoded key

        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int netId = wifiManager.addNetwork(conf);

        wifiManager.disconnect();

        if (netId != -1) {
            //Add the network to the known networks list for the first time
            wifiManager.enableNetwork(netId, true);
        } else if ((netId = getExistingNetworkId(conf.SSID, wifiManager)) != -1) {
            //network already known, look it up in the existing networks list
            wifiManager.enableNetwork(netId, true);
        } else
            return false;

        wifiManager.reconnect();
        return true;
    }

    private static int getExistingNetworkId(String SSID, WifiManager wifiManager) {

        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (existingConfig.SSID.replaceAll("^\"|\"$", "").equals(SSID.replaceAll("^\"|\"$", ""))) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }
}
