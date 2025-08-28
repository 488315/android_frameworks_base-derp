package com.android.systemui.power

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import com.android.systemui.res.R

/** Host activity for the performance profile settings fragment. */
class PerformanceProfileActivity : CollapsingToolbarBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(com.android.settingslib.widget.theme.R.style.Theme_SubSettingsBase)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PerformanceProfileFragment())
                .commit()
        }
    }
}
