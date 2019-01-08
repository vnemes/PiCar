package vendetta.blecar.http.requests;

/**
 * Created by Vendetta on 27-May-18.
 */

public enum ServiceEnum {
    GPS_SERVICE, ULTRASONIC_SERVICE, CONTROLLER_SERVICE, PICAMERA_SERVICE;

    public static String getValue(ServiceEnum value) {
        switch (value) {
            case GPS_SERVICE:
                return "gps";
            case PICAMERA_SERVICE:
                return "picamera";
            case CONTROLLER_SERVICE:
                return "picar-controller";
            case ULTRASONIC_SERVICE:
                return "ultrasonic-sensor";
            default:
                return "not_implemented";
        }
    }
    }
