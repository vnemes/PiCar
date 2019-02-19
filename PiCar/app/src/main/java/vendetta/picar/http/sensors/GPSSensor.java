package vendetta.picar.http.sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import vendetta.picar.ControllerActivity;
import vendetta.picar.R;
import vendetta.picar.http.HTTPHandlerSingleton;
import vendetta.picar.http.HTTPRequest;

/**
 * Created by Vendetta on 27-May-18.
 */

public class GPSSensor extends HTTPRequest implements ISensor {

    private GPSData gpsData;

    class GPSData {
        public double latitude;
        public double longitude;
        public double altitude;
        public double real;

        public GPSData(double latitude, double longitude, double altitude, double real) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.real = real;
        }
    }


    public GPSSensor(Context context) {
        super(context);
    }


    @Override
    public void requestData() {
        jsonRequest(Request.Method.GET, IP + context.getString(R.string.api_gps_endpoint), null, response -> {
            Log.d("HTTP", response.toString());
            try {
                gpsData = new GPSData((double) response.get("latitude"), (double) response.get("longitude"), (double) response.get("altitude"), (double) response.get("real"));
                if (gpsData.real == 1.0) {
                    ((ControllerActivity) context).showMap(gpsData.latitude, gpsData.longitude);
//                    Toast.makeText(context, "Lat:"+gpsData.latitude+" long:"+gpsData.longitude+" alt:"+gpsData+'m', Toast.LENGTH_SHORT).show();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    preferences.edit().putFloat(context.getString(R.string.gps_prev_lat), (float) gpsData.latitude).putFloat(context.getString(R.string.gps_prev_lon), (float) gpsData.longitude).apply();
                } else requestDefaultMap(gpsData.latitude, gpsData.longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d("HTTP", error.toString());
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(super.context);
            double lat = preferences.getFloat(context.getString(R.string.gps_prev_lat), 45.747409f);
            double lon = preferences.getFloat(context.getString(R.string.gps_prev_lon), 21.226300f);
            requestDefaultMap(lat, lon);
            preferences.edit().putFloat(context.getString(R.string.gps_prev_lat), (float) lat).putFloat(context.getString(R.string.gps_prev_lon), (float) lon).apply();
        });
    }

    @Override
    public void enableDisableSensor(boolean enable) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("enabled", String.valueOf(enable));
            jsonRequest(Request.Method.POST, IP + context.getString(R.string.api_gps_endpoint), jsonObject, null, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestDefaultMap(double lat, double lon) {
        ((ControllerActivity) context).showMap(lat, lon);
        Toast.makeText(context, "No GPS coverage. Falling back to the last known position", Toast.LENGTH_LONG).show();
    }

    public GPSData getGpsData() {
        return gpsData;
    }
}
