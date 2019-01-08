package vendetta.blecar.http.sensors;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import vendetta.blecar.ControllerActivity;
import vendetta.blecar.http.HTTPHandlerSingleton;
import vendetta.blecar.http.HTTPRequest;

/**
 * Created by Vendetta on 27-May-18.
 */

public class GPSSensor extends HTTPRequest implements ISensor {

    private GPSData gpsData;

    class GPSData{
        public double latitude;
        public double longitude;
        public double altitude;
        public double real;

        public GPSData(double latitude, double longitude, double altitude,double real) {
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
        HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.GET, IP + "/sensor/gps/", null, response -> {
            Log.d("HTTP",response.toString());
            try {
                //todo add callback to ControllerActivity to launch a dialogbox with a map with received coords
                gpsData = new GPSData((double) response.get("latitude"),(double) response.get("longitude"),(double) response.get("altitude"), (double)response.get("real"));
                if (gpsData.real == 1.0) {
                    ((ControllerActivity) context).showMap(gpsData.latitude, gpsData.longitude);
                    Toast.makeText(context, "Lat:"+gpsData.latitude+" long:"+gpsData.longitude+" alt:"+gpsData+'m', Toast.LENGTH_SHORT).show();
                }
                else requestDefaultMap(gpsData.latitude,gpsData.longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d("HTTP",error.toString());
            requestDefaultMap(45.747409,21.226300);
        }));
    }

    private void requestDefaultMap(double lat, double lon){
        ((ControllerActivity)context).showMap(lat,lon);
        Toast.makeText(context, "No GPS coverage. Falling back to the last known position", Toast.LENGTH_LONG).show();
    }

    public GPSData getGpsData() {
        return gpsData;
    }
}
