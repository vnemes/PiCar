package vendetta.blecar

import android.os.Bundle
import android.app.Activity

import kotlinx.android.synthetic.main.activity_settings.*
import vendetta.blecar.preferences.SettingsFragment

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        fragmentManager.beginTransaction()
                .replace(android.R.id.content,   SettingsFragment())
//                .addToBackStack(null)
                .commit()
    }

}
