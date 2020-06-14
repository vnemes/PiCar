package vendetta.picar.http.requests;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import vendetta.picar.ControllerActivity;
import vendetta.picar.R;
import vendetta.picar.http.HTTPHandlerSingleton;
import vendetta.picar.http.HTTPRequest;
import vendetta.picar.connection.ConnectionStateEn;

/**
 * Created by Vendetta on 26-May-18.
 */

public class HealthRequest extends HTTPRequest {


    private static final int CYCLIC_HEALTH_CHECK_INTERVAL = 1500; // milliseconds

    public HealthRequest(Context context, String IP) {
        super(context, IP);
    }

    public void cyclicHealthCheck(Handler handler) {
        HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.GET, IP + context.getString(R.string.api_health_endpoint), null,
                response -> handler.postDelayed(() -> cyclicHealthCheck(handler), CYCLIC_HEALTH_CHECK_INTERVAL),
                error -> {
                    Log.d("HTTP", error.toString());
                    ((ControllerActivity) context).onConnectionChange(ConnectionStateEn.DISCONNECTED);
                }));
    }

}
