package vendetta.blecar

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import vendetta.blecar.preferences.ConnectionConfig
import java.util.*

class ConnectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        // todo refactor to ConnectionConfig
        val arr:List<String> = Arrays.asList("sunt","un","boss")
        var arrAdapter:ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, arr)
        val listView:ListView = findViewById(R.id.conn_ListView)
        listView.adapter = arrAdapter
    }
}
