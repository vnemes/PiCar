package vendetta.picar.http.requests;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import vendetta.picar.ControllerActivity;
import vendetta.picar.http.HTTPHandlerSingleton;
import vendetta.picar.http.HTTPRequest;
import vendetta.picar.connection.ConnectionStateEn;

/**
 * Created by Vendetta on 26-May-18.
 */

public class HealthCheckCyclicRequest extends HTTPRequest {


    public HealthCheckCyclicRequest(Context context, String IP) {
        super(context, IP);
    }

    public void connect(Handler handler) {
        HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.GET, IP + "/sensor/demo/", null,
                response -> {
                    Log.d("HTTP", response.toString());


                    try {
                        String resp = (String) response.get("sensor_name");
                        if (resp.equals("demo_sensor"))
                            handler.postDelayed(() -> connect(handler), 1500);
                        else
                            ((ControllerActivity) context).onConnectionChange(ConnectionStateEn.DISCONNECTED);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("HTTP", error.toString());
                    ((ControllerActivity) context).onConnectionChange(ConnectionStateEn.DISCONNECTED);
                }));
    }
}
