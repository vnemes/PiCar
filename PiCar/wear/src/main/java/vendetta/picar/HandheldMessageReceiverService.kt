package vendetta.picar

import android.content.Intent
import com.google.android.gms.wearable.WearableListenerService
import android.support.v4.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent



class HandheldMessageReceiverService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent?) {

        if (messageEvent!!.path == getString(R.string.wear_connection_path)) {

            // retrieve the message
            val message = String(messageEvent.data)
            val messageIntent = Intent()
            messageIntent.action = Intent.ACTION_SEND
            messageIntent.putExtra(getString(R.string.handheld_conn_change), message)

            // Broadcast the received Data Layer messages locally
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
        } else {
            super.onMessageReceived(messageEvent)
        }
    }
}
