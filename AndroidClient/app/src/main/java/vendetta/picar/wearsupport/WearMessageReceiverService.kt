package vendetta.picar.wearsupport

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import vendetta.picar.R

class WearMessageReceiverService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {

        if (messageEvent!!.path == getString(R.string.wear_connection_path)) {

            val message = String(messageEvent.data)
            val messageIntent = Intent()
            messageIntent.action = Intent.ACTION_SEND
            val anglStr = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            messageIntent.putExtra(getString(R.string.wear_set_angle), Integer.valueOf(anglStr[0]))
            messageIntent.putExtra(getString(R.string.wear_set_strength), Integer.valueOf(anglStr[1]))
            // Broadcast the received Data Layer messages locally
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
        } else {
            super.onMessageReceived(messageEvent)
        }
    }

}