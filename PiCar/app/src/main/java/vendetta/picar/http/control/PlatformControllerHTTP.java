package vendetta.picar.http.control;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import vendetta.picar.R;
import vendetta.picar.http.HTTPHandlerSingleton;
import vendetta.picar.http.HTTPRequest;

public class PlatformControllerHTTP extends HTTPRequest {

    public PlatformControllerHTTP(Context context, String IP) {
        super(context, IP);
    }

    public void enableDisablePlatform(boolean enable) {
        enableDisablePlatform(enable, null, null);
    }

    public void enableDisablePlatform(boolean enable, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("enabled", String.valueOf(enable));
            jsonObject.put("platform", "SHELBYGT500");
            jsonRequest(Request.Method.POST, IP + context.getString(R.string.api_control_endpoint), jsonObject, listener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
