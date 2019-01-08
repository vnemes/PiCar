package vendetta.blecar.http.requests;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import vendetta.blecar.http.HTTPHandlerSingleton;
import vendetta.blecar.http.HTTPRequest;

/**
 * Created by Vendetta on 27-May-18.
 */

public class ServiceRequest extends HTTPRequest {

    private static JSONObject jsonObject = new JSONObject();

    public ServiceRequest(Context context, String IP) {
        super(context, IP);
    }

    public void request(ServiceEnum serviceEnum, CommandEnum commandEnum) {
        try {
            jsonObject.put("service", ServiceEnum.getValue(serviceEnum));
            jsonObject.put("cmd", CommandEnum.getValue(commandEnum));
            Log.d(getClass().getSimpleName(), jsonObject.toString());
            HTTPHandlerSingleton.getInstance(super.context).addToRequestQueue(new JsonObjectRequest(Request.Method.POST, super.IP + "/administration/service/", jsonObject, null, null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void requestForCallback(ServiceEnum serviceEnum, CommandEnum commandEnum, Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        try {
            jsonObject.put("service", ServiceEnum.getValue(serviceEnum));
            jsonObject.put("cmd", CommandEnum.getValue(commandEnum));
            Log.d(getClass().getSimpleName(), jsonObject.toString());
            HTTPHandlerSingleton.getInstance(context).addToRequestQueue(new JsonObjectRequest(Request.Method.POST, IP + "/administration/service/", jsonObject, listener, errorListener));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
