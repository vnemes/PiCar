package vendetta.picar.http.sensors;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
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
 * Created by Vendetta on 02-May-18.
 */

public class UltrasonicSensor extends HTTPRequest implements ISensor {

    private double distance;


    public UltrasonicSensor(Context context) {
        super(context);
        this.distance = Double.NaN;
    }

    public void requestData() {
        jsonRequest(Request.Method.GET, IP + context.getString(R.string.api_ultrasonic_endpoint), null, response -> {
            Log.d("HTTP", response.toString());
            try {
                distance = (double) response.get("distance");
                ((ControllerActivity) context).updateDistanceTV(distance);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d("HTTP", error.toString());
        });
    }

    @Override
    public void enableDisableSensor(boolean enable) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("enabled", String.valueOf(enable));
            jsonRequest(Request.Method.POST, IP + context.getString(R.string.api_ultrasonic_endpoint), jsonObject, null, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
