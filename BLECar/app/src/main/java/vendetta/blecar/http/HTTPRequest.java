package vendetta.blecar.http;

import android.content.Context;

/**
 * Created by Vendetta on 27-May-18.
 */

public class HTTPRequest {

    protected Context context;
    protected String IP;

    public HTTPRequest(Context context) {
        this.context = context;
    }

    public HTTPRequest(Context context, String IP) {
        this.context = context;
        this.IP = IP;
    }

    public void setIp(String IP){
        this.IP = IP;
    }

    public String getIP() {
        return IP;
    }
}
