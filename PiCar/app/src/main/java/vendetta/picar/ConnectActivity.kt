package vendetta.picar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vendetta.picar.connection.ConnectionConfig
import vendetta.picar.connection.ConnectionTypeEn
import kotlin.collections.ArrayList
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.*
import vendetta.picar.preferences.EditConnDialogFragment


class ConnectActivity : Activity(), EditConnDialogFragment.IConnEditable {
    private val connectionListView: ListView by lazy { findViewById<ListView>(R.id.connectionListView) }
    private val connectionIV: ImageView by lazy { findViewById<ImageView>(R.id.ConnectionIV) }
    private val connectionNameTV: TextView by lazy { findViewById<TextView>(R.id.connectionNameTV) }
    private val connectionSpecificValueTV: TextView by lazy { findViewById<TextView>(R.id.connectionSpecificValueTV) }
    private val connectionSpecificIdTV: TextView by lazy { findViewById<TextView>(R.id.connectionSpecificIdTV) }
    private val connectionTypeTV: TextView by lazy { findViewById<TextView>(R.id.connectionTypeTV) }
    private lateinit var selectedConnection: ConnectionConfig
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var connectionArr: MutableList<ConnectionConfig>

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)


        // check if the application is started for the first time
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE) ?: return
        val firstRun = sharedPreferences.getBoolean(getString(R.string.pref_key_first_launch), true)
        if (firstRun) {
            // initialize connection list with default values
            connectionArr = mutableListOf(
                    ConnectionConfig("PiZeroW Home", ConnectionTypeEn.WIFI_LOCAL, "192.168.100.18","",""),
                    ConnectionConfig("Pi3B+ DynDNS", ConnectionTypeEn.WIFI_INET, "vendettapi.go.ro","",""),
                    ConnectionConfig("PiZeroW AP Local", ConnectionTypeEn.WIFI_AP, "PiZeroCar","192.168.10.1","P1Password"),
                    ConnectionConfig("PiZeroW BLE", ConnectionTypeEn.BLE, "BLECar","",""),
                    ConnectionConfig("PiZeroW MQTT", ConnectionTypeEn.MQTT, "192.168.43.38","",""),
                    ConnectionConfig("PiZeroW DynDNS", ConnectionTypeEn.WIFI_INET, "vendettapi.go.ro","",""),
                    ConnectionConfig("PiZeroW HotSpot", ConnectionTypeEn.WIFI_LOCAL, "192.168.43.38","",""))

            saveConnectionArray()
            sharedPreferences
                    .edit()
                    .putBoolean(getString(R.string.pref_key_first_launch), false)
                    .commit()

        } else {
            // retrieve connection list from preferences and deserialize
            val listType = object : TypeToken<ArrayList<ConnectionConfig>>() {}.type
            connectionArr = Gson().fromJson(sharedPreferences.getString(getString(R.string.pref_key_connection_config), ""), listType)
        }

        // override array adapter to account for name and connection type
        val arrAdapter = object : ArrayAdapter<ConnectionConfig>(this, android.R.layout.simple_list_item_2, android.R.id.text1, connectionArr) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val text1 = view.findViewById(android.R.id.text1) as TextView
                val text2 = view.findViewById(android.R.id.text2) as TextView


                text1.text = connectionArr[position].name
                text1.setTextAppearance(android.R.style.TextAppearance_Holo_Medium)
                text2.text = connectionArr[position].connType.value
                text2.setTextColor(ContextCompat.getColor(context, R.color.inactiveTextColor))
                return view
            }
        }

        connectionListView.adapter = arrAdapter
        // populate content pane with information depending on selection
        connectionListView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            updateSelectedConnectionPanel(arrAdapter.getItem(position))
        }
        // select first item in the list (highlighting does not work!)
        connectionListView.performItemClick(connectionListView, 0, arrAdapter.getItemId(0))
    }

    private fun saveConnectionArray() {
        // serialize to store as string
        val connectJson = Gson().toJson(connectionArr)
        sharedPreferences
                .edit()
                .putString(getString(R.string.pref_key_connection_config), connectJson)
                .apply()
    }

    private fun updateSelectedConnectionPanel(desiredConnection : ConnectionConfig) {
        selectedConnection = desiredConnection
        connectionNameTV.text = selectedConnection.name
        connectionSpecificValueTV.text = selectedConnection.identifier
        connectionTypeTV.text = selectedConnection.connType.value
        connectionSpecificIdTV.text = selectedConnection.connType.specific
        connectionIV.setImageResource(selectedConnection.connType.img)
    }

    fun onSettingsBtnPress(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun onEditBtnPress(view: View) {
        val newFragment = EditConnDialogFragment()
        newFragment.show(fragmentManager, "dialog")
    }

    override fun onSaveButtonPress(connectionConfig: ConnectionConfig) {
        connectionArr[connectionArr.indexOf(selectedConnection)] = connectionConfig
        val tmpAdapter: ArrayAdapter<ConnectionConfig> = connectionListView.adapter as ArrayAdapter<ConnectionConfig>
        tmpAdapter.notifyDataSetChanged()
        updateSelectedConnectionPanel(connectionConfig)
        saveConnectionArray()
    }

    fun onConnectBtnPress(view: View){
        val intent = Intent(this, ControllerActivity::class.java)
        intent.putExtra(getString(R.string.selected_connection_json_key),  Gson().toJson(selectedConnection))
        startActivity(intent)
    }

    override fun getActiveSelection(): ConnectionConfig {
        return selectedConnection
    }
}
