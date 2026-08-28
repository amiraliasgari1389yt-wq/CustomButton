package com.custombutton.app

import android.content.Context

object PrefsHelper {

    private const val PREFS_NAME = "home_launch_prefs"
    private const val KEY_TARGET_PACKAGE = "target_package"
    private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
    private const val KEY_LENGTH_PERCENT = "length_percent"
    private const val KEY_HOLD_DURATION_MS = "hold_duration_ms"

    const val DEFAULT_LENGTH_PERCENT = 100
    const val DEFAULT_HOLD_DURATION_MS = 500

    fun getTargetPackage(context: Context): String? {
        return prefs(context).getString(KEY_TARGET_PACKAGE, null)
    }

    fun setTargetPackage(context: Context, packageName: String) {
        prefs(context).edit().putString(KEY_TARGET_PACKAGE, packageName).apply()
    }

    fun isOverlayEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_OVERLAY_ENABLED, false)
    }

    fun setOverlayEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
    }

    fun getLengthPercent(context: Context): Int {
        return prefs(context).getInt(KEY_LENGTH_PERCENT, DEFAULT_LENGTH_PERCENT)
    }

    fun setLengthPercent(context: Context, percent: Int) {
        prefs(context).edit().putInt(KEY_LENGTH_PERCENT, percent).apply()
    }

    fun getHoldDurationMs(context: Context): Int {
        return prefs(context).getInt(KEY_HOLD_DURATION_MS, DEFAULT_HOLD_DURATION_MS)
    }

    fun setHoldDurationMs(context: Context, ms: Int) {
        prefs(context).edit().putInt(KEY_HOLD_DURATION_MS, ms).apply()
    }

    fun prefsName(): String = PREFS_NAME
    fun lengthKey(): String = KEY_LENGTH_PERCENT
    fun holdDurationKey(): String = KEY_HOLD_DURATION_MS

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
