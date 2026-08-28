package com.custombutton.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val wasEnabled = PrefsHelper.isOverlayEnabled(context)
        val hasPermission = Settings.canDrawOverlays(context)

        if (wasEnabled && hasPermission) {
            OverlayService.start(context)
        }
    }
}
