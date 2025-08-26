package com.android.systemui.power

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings

/** Listens for performance profile changes. */
class PerformanceProfileListener(
    private val context: Context,
    private val handler: Handler
) {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            update()
        }
    }

    private val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            update()
        }
    }

    fun start() {
        val filter = IntentFilter(PowerManager.ACTION_PERFORMANCE_PROFILE_CHANGED)
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.PERFORMANCE_PROFILE_MODE),
            false, observer)
        update()
    }

    private fun update() {
        Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.PERFORMANCE_PROFILE_MODE,
            0)
    }
}
