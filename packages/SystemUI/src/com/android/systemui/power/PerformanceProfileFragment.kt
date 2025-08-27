package com.android.systemui.power

import android.os.Bundle
import android.os.PowerManager
import androidx.preference.PreferenceFragmentCompat
import com.android.settingslib.widget.SelectorWithWidgetPreference
import com.android.systemui.res.R

/** Settings screen for selecting the performance profile mode. */
class PerformanceProfileFragment : PreferenceFragmentCompat(),
    SelectorWithWidgetPreference.OnClickListener {

    private lateinit var standardPref: SelectorWithWidgetPreference
    private lateinit var lightPref: SelectorWithWidgetPreference
    private lateinit var powerManager: PowerManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.performance_profile_settings)
        standardPref = requireNotNull(findPreference("performance_profile_standard"))
        lightPref = requireNotNull(findPreference("performance_profile_light"))
        standardPref.setOnClickListener(this)
        lightPref.setOnClickListener(this)
        powerManager = requireContext().getSystemService(PowerManager::class.java)!!
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(R.string.performance_optimization)
        updateChecks(powerManager.getPerformanceProfileMode())
    }

    override fun onRadioButtonClicked(emiter: SelectorWithWidgetPreference) {
        val mode = when (emiter) {
            standardPref -> 0
            lightPref -> 1
            else -> return
        }
        powerManager.setPerformanceProfileMode(mode)
        updateChecks(mode)
    }

    private fun updateChecks(mode: Int) {
        standardPref.isChecked = mode == 0
        lightPref.isChecked = mode == 1
    }
}
