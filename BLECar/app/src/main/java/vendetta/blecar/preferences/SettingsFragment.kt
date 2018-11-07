package vendetta.blecar.preferences

import android.os.Bundle
import android.preference.PreferenceFragment
import vendetta.blecar.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }
}