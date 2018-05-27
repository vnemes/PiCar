package vendetta.blecar.requests;

/**
 * Created by Vendetta on 27-May-18.
 */

public enum CommandEnum {
    START, STOP, RESTART, STATUS;

    public static String getValue(CommandEnum commandEnum) {
        switch (commandEnum) {
            case STOP:
                return "stop";
            case START:
                return "start";
            case STATUS:
                return "status";
            case RESTART:
                return "restart";
            default:
                return "wrong_command";
        }
    }
}
