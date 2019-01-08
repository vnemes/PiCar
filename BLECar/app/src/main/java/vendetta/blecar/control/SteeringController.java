package vendetta.blecar.control;

import android.content.Context;

import vendetta.blecar.ControllerActivity;

/**
 * Created by Vendetta on 06-May-18.
 */

public abstract class SteeringController {

    protected Context context;
    private int lastSteerStr;
    private int lastSteerAngl;

    public SteeringController(Context context) {
        this.context = context;
        lastSteerStr = 0;
        lastSteerAngl = 0;
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

    protected void setSteering(int steering, int direction) {

    }
}
