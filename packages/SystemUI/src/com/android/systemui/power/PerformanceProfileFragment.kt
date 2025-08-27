package com.android.systemui.power

import android.os.Bundle
import android.os.PowerManager
import androidx.preference.PreferenceFragment
import com.android.settingslib.widget.SelectorWithWidgetPreference
import com.android.systemui.res.R

/** Settings screen for selecting the performance profile mode. */
class PerformanceProfileFragment : PreferenceFragment(),
    SelectorWithWidgetPreference.OnClickListener {

    private lateinit var standardPref: SelectorWithWidgetPreference
    private lateinit var lightPref: SelectorWithWidgetPreference
    private lateinit var powerManager: PowerManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.performance_profile_settings)
        standardPref = findPreference("performance_profile_standard")!!
        lightPref = findPreference("performance_profile_light")!!
        standardPref.setOnClickListener(this)
        lightPref.setOnClickListener(this)
        powerManager = requireContext().getSystemService(PowerManager::class.java)
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.performance_optimization)
        updateChecks(powerManager.getPerformanceProfileMode())
    }

    override fun onRadioButtonClicked(emiter: SelectorWithWidgetPreference) {
        val mode = if (emiter === standardPref) 0 else 1
        powerManager.setPerformanceProfileMode(mode)
        updateChecks(mode)
    }

    private fun updateChecks(mode: Int) {
        standardPref.isChecked = mode == 0
        lightPref.isChecked = mode == 1
    }
}
