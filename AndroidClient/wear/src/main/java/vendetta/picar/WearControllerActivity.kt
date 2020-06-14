package vendetta.picar

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.wearable.Node
import io.github.controlwear.virtual.joystick.android.JoystickView
import java.lang.Exception
import java.util.concurrent.ExecutionException
import kotlin.concurrent.thread


class WearControllerActivity : WearableActivity() {

    private val textView: TextView by lazy { findViewById<TextView>(R.id.text) }
    private val joystickSpeed: JoystickView by lazy { findViewById<JoystickView>(R.id.joystick) }
    private val exitButton: Button by lazy { findViewById<Button>(R.id.button_exit) }
    private val JOYSTICK_UPDATE_INTERVAL = 500
    private var messageReceiver: BroadcastReceiver ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_controller)
        setAmbientEnabled() // make app force screen on

        textView.text = ConnectionStateEn.DISCONNECTED.name
        textView.setTextColor(Color.RED)
        joystickSpeed.isEnabled = false
        joystickSpeed.visibility = View.INVISIBLE
        joystickSpeed.buttonDirection = 0 // both directions
        joystickSpeed.setOnMoveListener({ angle, strength -> sendMovementCommand(angle, strength) }, JOYSTICK_UPDATE_INTERVAL)

        exitButton.setOnClickListener { LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver); finish();}
        //Register to receive local broadcasts from messageService
        val newFilter = IntentFilter(Intent.ACTION_SEND)
        messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val connState = ConnectionStateEn.valueOf(intent.getStringExtra(getString(R.string.handheld_conn_change)))
                joystickSpeed.isEnabled = connState == ConnectionStateEn.CONNECTED
                joystickSpeed.visibility = if (connState == ConnectionStateEn.CONNECTED) View.VISIBLE else View.INVISIBLE
                textView.text = connState.name
                textView.setTextColor(if (connState == ConnectionStateEn.CONNECTED) Color.GREEN else if (connState == ConnectionStateEn.CONNECTING) Color.YELLOW else Color.RED)
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter)

    }

    private fun sendMovementCommand(angle: Int, strength: Int) {
        val movementCommand = "$angle:$strength"
        //Use the same path as the handheld device
        val dataPath = getString(R.string.wear_connection_path)

        thread {
            val nodeListTask = Wearable.getNodeClient(applicationContext).connectedNodes
            try {
                //Block on a task and get the result synchronously
                val nodes = Tasks.await<List<Node>>(nodeListTask)
                for (node in nodes) {
                    //Send the message///
                    val sendMessageTask = Wearable.getMessageClient(this@WearControllerActivity).sendMessage(node.id, dataPath, movementCommand.toByteArray())
                    Tasks.await(sendMessageTask)
                }
            } catch (e: Exception) {
                when (e) {
                    is ExecutionException -> {
                        Toast.makeText(this, "Error sending command", Toast.LENGTH_SHORT).show()
                    }
                    is InterruptedException -> Toast.makeText(this, "Error contacting handheld", Toast.LENGTH_SHORT).show()
                    else -> throw e
                }
            }
        }
    }

}
