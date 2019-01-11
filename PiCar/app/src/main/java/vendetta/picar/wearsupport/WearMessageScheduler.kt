package vendetta.picar.wearsupport

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import vendetta.picar.R
import vendetta.picar.connection.ConnectionStateEn
import java.util.concurrent.ExecutionException
import kotlin.concurrent.thread

class WearMessageScheduler {
    companion object {
        const val TAG = "WEAR"

        @JvmStatic
        fun scheduleStateChangeMessage(context: Context, connectionStateEn: ConnectionStateEn) {
            val message = connectionStateEn.toString()
            Log.d(TAG, "Sending message: $message")

            thread {
                // Retrieve the connected devices (nodes)
                val wearableList = Wearable.getNodeClient(context).connectedNodes
                try {
                    val nodes = Tasks.await(wearableList)
                    for (node in nodes) {
                        val sendMessageTask = Wearable.getMessageClient(context).sendMessage(node.id, context.getString(R.string.wear_connection_path), message.toByteArray())
                        try {
                            // Block on a task and get the result synchronously
                            Tasks.await(sendMessageTask)
                        } catch (exception: ExecutionException) {
                            Log.d(TAG, "Error on sending message")
                        } catch (exception: InterruptedException) {
                            Log.d(TAG, "Error on sending message")
                        }
                    }
                } catch (exception: ExecutionException) {
                    Log.d(TAG, "No wearable present")
                } catch (exception: InterruptedException) {
                    Log.d(TAG, "No wearable present")
                }
            }
        }
    }
}