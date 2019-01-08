package vendetta.picar.http.control;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

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
            HTTPHandlerSingleton.getInstance(super.context).addToRequestQueue(new JsonObjectRequest(Request.Method.POST,request.getIP() + "/control/speed/",jsonObject,null,null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
