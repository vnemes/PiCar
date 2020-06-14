package vendetta.picar.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import vendetta.picar.ControllerActivity;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Vendetta on 29-Apr-18.
 */

public class PiWiFiManager {
    private static String TAG = PiWiFiManager.class.getSimpleName();

    private static BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ControllerActivity activity = (ControllerActivity) context;

            NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Log.d(TAG, netInfo.toString());

            if (netInfo.getState().equals(NetworkInfo.State.CONNECTED)
                    && netInfo.getType() == ConnectivityManager.TYPE_WIFI
                    && netInfo.getExtraInfo().replaceAll("^\"|\"$", "").equals(activity.getConfig().getIdentifier())) {
                activity.cancelWifiApTimout();
                activity.establishConnection(activity.getConfig().getIdentifier());
            }
        }
    };

    public static BroadcastReceiver getReceiver() {
        return receiver;
    }

    public static IntentFilter getFilter() {
        return new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    public static void connectToWiFiAP(Context context, String ssid, String pw) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);

        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = String.format("\"%s\"", ssid);
            conf.preSharedKey = String.format("\"%s\"", pw);

            wifiManager.addNetwork(conf);
            int netId = getExistingNetworkId(conf.SSID, wifiManager);
            wifiManager.enableNetwork(netId, true);
        } else
            Toast.makeText(context, "Something went wrong whilst trying to access the WiFi API", Toast.LENGTH_SHORT).show();
    }

    private static int getExistingNetworkId(String ssid, WifiManager wifiManager) {

        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (existingConfig.SSID.replaceAll("^\"|\"$", "").equals(ssid.replaceAll("^\"|\"$", ""))) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }
}
