package vendetta.picar.http.sensors;

/**
 * Created by Vendetta on 06-May-18.
 */

public interface ISensor {

    void requestData();

    void enableDisableSensor(boolean enable);

}
