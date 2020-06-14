package vendetta.picar.http.control;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import vendetta.picar.R;
import vendetta.picar.http.HTTPRequest;

public class CollAvoidControllerHTTP extends HTTPRequest {

    public CollAvoidControllerHTTP(Context context, String IP) {
        super(context, IP);
    }

    public void enableDisableACC(boolean enable){
        enableDisableAcc(enable, null, null);
    }

    public void enableDisableAcc(boolean enable, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("enabled", String.valueOf(enable));
            jsonRequest(Request.Method.POST, IP + context.getString(R.string.api_coll_av_endpoint), jsonObject, listener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
