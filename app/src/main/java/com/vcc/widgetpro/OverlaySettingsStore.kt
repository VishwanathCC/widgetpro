package com.vcc.widgetpro

import android.content.Context

class OverlaySettingsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOverlayFallbackEnabled(): Boolean {
        return prefs.getBoolean(KEY_OVERLAY_FALLBACK, false)
    }

    fun setOverlayFallbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_FALLBACK, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "caffeinate_overlay_settings"
        private const val KEY_OVERLAY_FALLBACK = "overlay_fallback"
    }
}
