package vendetta.picar.http.control;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import vendetta.picar.R;
import vendetta.picar.http.HTTPRequest;

public class LaneKeepAssistantHTTP extends HTTPRequest {

    public LaneKeepAssistantHTTP(Context context, String IP) {
        super(context, IP);
    }

    public void enableDisableLKA(boolean enable){
        enableDisableLKA(enable, null, null);
    }

    public void enableDisableLKA(boolean enable, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("enabled", String.valueOf(enable));
            jsonRequest(Request.Method.POST, IP + context.getString(R.string.api_lka_endpoint), jsonObject, listener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}