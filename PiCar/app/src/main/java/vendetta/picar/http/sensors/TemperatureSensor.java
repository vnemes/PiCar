package vendetta.picar.http.sensors;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import vendetta.picar.ControllerActivity;
import vendetta.picar.R;
import vendetta.picar.http.HTTPHandlerSingleton;
import vendetta.picar.http.HTTPRequest;

public class TemperatureSensor extends HTTPRequest implements ISensor {

    private double temperature;

    public TemperatureSensor(Context context) {
        super(context);
        this.temperature = Double.NaN;
    }

    public void requestData() {
        HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.GET, IP + context.getString(R.string.api_temp_endpoint), null, response -> {
            Log.d("HTTP", response.toString());
            try {
                temperature = (double) response.get("temperature");
//                ((ControllerActivity)context).updateDistanceTV(distance);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.d("HTTP", error.toString())));
    }

    @Override
    public void enableDisableSensor(boolean enable) {

// not implemented on platform, always on-----------
    }
}
