package vendetta.blecar.requests;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import vendetta.blecar.ControllerActivity;
import vendetta.blecar.R;
import vendetta.blecar.http.HTTPHandlerSingleton;
import vendetta.blecar.http.HTTPRequest;
import vendetta.blecar.http.WiFiStateEnum;

/**
 * Created by Vendetta on 26-May-18.
 */

public class CheckConnectionRequest extends HTTPRequest{


    public CheckConnectionRequest(Context context,String IP) {
        super(context,IP);
    }

    public void connect(){
        HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.GET, IP + "/sensor/demo/", null, response -> {
            Log.d("HTTP",response.toString());
            try {
                String resp = (String) response.get("sensor_name");
                if (resp.equals("demo_sensor"))
                    ((ControllerActivity)context).onConnectionChange(WiFiStateEnum.CONNECTED);
                else ((ControllerActivity)context).onConnectionChange(WiFiStateEnum.DISCONNECTED);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d("HTTP",error.toString());
            ((ControllerActivity)context).onConnectionChange(WiFiStateEnum.DISCONNECTED);
            Toast.makeText(context, "Cannot connect to " + IP, Toast.LENGTH_SHORT).show();
        }));
    }
}
