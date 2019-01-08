package vendetta.blecar.http.control;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import vendetta.blecar.control.SteeringController;
import vendetta.blecar.http.HTTPHandlerSingleton;
import vendetta.blecar.http.HTTPRequest;

public class SteeringControllerHTTP extends SteeringController {
    private HTTPRequest request;
    private static JSONObject jsonObject;

    public SteeringControllerHTTP(Context context, String ip){
        super(context);
        request = new HTTPRequest(context, ip);
        jsonObject = new JSONObject();
    }

    @Override
    protected void setSteering(int steering, int direction) {
        try {
            jsonObject.put("steering", steering);
            jsonObject.put("direction", direction);
            Log.d(getClass().getSimpleName(), jsonObject.toString());
            HTTPHandlerSingleton.getInstance(super.context).addToRequestQueue(new JsonObjectRequest(Request.Method.POST, request.getIP() + "/control/steering/", jsonObject, null, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
