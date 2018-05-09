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

/**
 * Created by Vendetta on 06-May-18.
 */

public class SpeedController {

    private Context context;
    private static JSONObject jsonObject = new JSONObject();


    private int lastSpeedStr;
    private int lastSpeedAngle;

    private int speedLimit;

    public SpeedController(Context context) {
        this.context = context;
        lastSpeedStr = 0;
        lastSpeedAngle = 0;
        speedLimit = 50;
        setSpeed(0,0);
    }

    public void setSpeedStrAngle(int angle, int strength){

        strength = strength < speedLimit ? strength : speedLimit;

        if (strength == lastSpeedStr && angle == lastSpeedAngle)
            return ;

        int direction = Math.sin(Math.toRadians(angle)) >= 0 ? 1 : 0;
        lastSpeedStr = strength;
        lastSpeedAngle = angle;

        setSpeed(strength,direction);

        ((ControllerActivity)context).updateCrtSpeedTV(direction == 1? strength : -strength);
    }

    public void setMaxSpeed(int maxspeed){
        speedLimit = maxspeed;
    }


    private void setSpeed(int speed, int direction){
        try {
            jsonObject.put("speed",speed);
            jsonObject.put("direction",direction);
            Log.d(getClass().getSimpleName(),jsonObject.toString());
            HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.POST,context.getString(R.string.pi_url) + "/control/speed/",jsonObject,null,null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
