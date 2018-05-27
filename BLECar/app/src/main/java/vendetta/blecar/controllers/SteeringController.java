package vendetta.blecar.controllers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import vendetta.blecar.ControllerActivity;
import vendetta.blecar.http.HTTPHandlerSingleton;
import vendetta.blecar.R;
import vendetta.blecar.http.HTTPRequest;

/**
 * Created by Vendetta on 06-May-18.
 */

public class SteeringController extends HTTPRequest {

    private static JSONObject jsonObject = new JSONObject();

    private int lastSteerStr;
    private int lastSteerAngl;

    public SteeringController(Context context) {
        super(context);
        lastSteerStr = 0;
        lastSteerAngl = 0;
//        setSteering(0, 0);
    }

    public void setSteeringAnglStr(int angle, int strength) {

        if (strength == lastSteerStr && angle == lastSteerAngl)
            return;

        int direction = Math.cos(Math.toRadians(angle)) >= 0 ? 0 : 1;
        lastSteerStr = strength;
        lastSteerAngl = angle;

        setSteering(strength, direction);

        ((ControllerActivity) context).updateCrtSteeringTV(direction == 0 ? strength : -strength);
    }

    private void setSteering(int steering, int direction) {
        try {
            jsonObject.put("steering", steering);
            jsonObject.put("direction", direction);
            Log.d(getClass().getSimpleName(), jsonObject.toString());
            HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.POST, IP + "/control/steering/", jsonObject, null, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
