package vendetta.picar.control;

import android.content.Context;

import vendetta.picar.ControllerActivity;

/**
 * Created by Vendetta on 06-May-18.
 */

public abstract class SpeedController {



    protected Context context;
    private int lastSpeedStr;
    private int lastSpeedAngle;

    private int speedLimit;

    public SpeedController(Context context) {
        this.context = context;
        lastSpeedStr = 0;
        lastSpeedAngle = 0;
        speedLimit = 50;
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

    public void setMaxSpeed(int maxSpeed){
        speedLimit = maxSpeed;
        requestSetSpeedLimit(maxSpeed);
    }

    protected void requestSetSpeedLimit(int maxSpeed){
    }


    protected void setSpeed(int speed, int direction){
    }
}
