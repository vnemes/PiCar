package vendetta.picar.http.sensors;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import vendetta.picar.ControllerActivity;
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

    public void requestData(){
        HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.GET, IP + "/sensor/ultrasonic/", null, response -> {
            Log.d("HTTP",response.toString());
            try {
                distance = (double) response.get("distance");
                ((ControllerActivity)context).updateDistanceTV(distance);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d("HTTP",error.toString());
        }));
    }

}
