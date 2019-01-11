package vendetta.picar.http;

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

    public static synchronized HTTPHandlerSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HTTPHandlerSingleton(context);
        }
        return mInstance;
    }

    private HTTPHandlerSingleton(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }


    public <T> void addToRequestQueue(Request<T> req) {
        requestQueue.add(req);
    }

}
