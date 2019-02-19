package vendetta.picar.http;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

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

    public void jsonRequest(int method, String url, JSONObject jsonRequest,
                            Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        HTTPHandlerSingleton
                .getInstance(context)
                .addToRequestQueue(new JsonObjectRequest(method, url, jsonRequest, listener, errorListener));
    }

    public void setIp(String IP) {
        this.IP = IP;
    }

    public String getIP() {
        return IP;
    }
}
