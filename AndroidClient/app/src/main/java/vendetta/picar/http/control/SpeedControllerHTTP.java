package vendetta.picar.http.control;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import vendetta.picar.R;
import vendetta.picar.control.SpeedController;
import vendetta.picar.http.HTTPHandlerSingleton;
import vendetta.picar.http.HTTPRequest;

public class SpeedControllerHTTP extends SpeedController {
    private HTTPRequest request;
    private static JSONObject jsonObject;

    public SpeedControllerHTTP(Context context, String ip) {
        super(context);
        request = new HTTPRequest(context, ip);
        jsonObject = new JSONObject();
    }

    @Override
    protected void setSpeed(int speed, int direction) {
        try {
            jsonObject.put("speed",speed);
            jsonObject.put("direction",direction);
            Log.d(getClass().getSimpleName(),jsonObject.toString());
            request.jsonRequest(Request.Method.POST,request.getIP() + context.getString(R.string.api_speed_endpoint),jsonObject,null,null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void requestSetSpeedLimit(int maxSpeed) {
        try {
            jsonObject.put("limit",maxSpeed);
            Log.d(getClass().getSimpleName(),jsonObject.toString());
            request.jsonRequest(Request.Method.POST,request.getIP() + context.getString(R.string.api_speed_endpoint)
                    + "/limit",jsonObject,null,null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
