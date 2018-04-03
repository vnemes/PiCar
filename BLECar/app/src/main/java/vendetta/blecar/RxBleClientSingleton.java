package vendetta.blecar;

import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.internal.RxBleLog;

/**
 * Created by Vendetta on 25-Feb-18.
 */

public class RxBleClientSingleton {
    private static RxBleClient mRxBleClient = null;

    public static RxBleClient getInstance(Context context) {
        if (mRxBleClient == null) {
            mRxBleClient = RxBleClient.create(context);
            RxBleClient.setLogLevel(RxBleLog.DEBUG); // Enable logging while debugging
        }
        return mRxBleClient;
    }

}
