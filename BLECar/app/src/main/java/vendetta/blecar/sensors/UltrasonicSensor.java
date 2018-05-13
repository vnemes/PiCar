package vendetta.blecar.sensors;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import vendetta.blecar.http.HTTPHandlerSingleton;
import vendetta.blecar.R;

/**
 * Created by Vendetta on 02-May-18.
 */

public class UltrasonicSensor implements ISensor {

    private Context context;
    private double distance;


    public UltrasonicSensor(Context context) {
        this.context = context;
        this.distance = Double.NaN;
    }

    public void requestData(){
        HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.GET, context.getString(R.string.pi_url) + "/sensor/ultrasonic/", null, response -> {
            Log.d("HTTP",response.toString());
            try {
                distance = (double) response.get("distance"); // todo add callback to ControllerActivity here
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d("HTTP",error.toString());
        }));
    }

}
