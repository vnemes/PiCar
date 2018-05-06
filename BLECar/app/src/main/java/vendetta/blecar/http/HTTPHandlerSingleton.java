package vendetta.blecar.http;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Vendetta on 02-May-18.
 */

public class HTTPHandlerSingleton {
    private static HTTPHandlerSingleton mInstance;
    private static RequestQueue requestQueue;
    private static Context mContext;

    public static synchronized HTTPHandlerSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HTTPHandlerSingleton(context);
        }
        return mInstance;
    }

    private HTTPHandlerSingleton(Context context) {
        mContext = context;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue(){
        if (requestQueue == null){
            requestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
