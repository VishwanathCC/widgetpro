package com.vcc.widgetpro

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Point
import android.os.Build
import android.view.Display
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class CaffeinateAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityFallbackHelper.buildServiceInfo()
        instance = this
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance === this) {
            instance = null
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    fun performKeepAwakePulse(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false

        val size = Point()
        @Suppress("DEPRECATION")
        val display: Display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display ?: return false
        } else {
            val windowManager = getSystemService(WindowManager::class.java)
            windowManager.defaultDisplay
        }
        @Suppress("DEPRECATION")
        display.getRealSize(size)

        // Best-effort only: this sends a very short gesture near the bottom edge to mimic subtle
        // user activity. Some apps/OEMs may ignore it, and some full-screen UIs may still react.
        val startX = (size.x * 0.08f).toInt().toFloat()
        val y = (size.y * 0.92f).toInt().toFloat()
        val path = Path().apply {
            moveTo(startX, y)
            lineTo(startX + 2f, y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 120))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    companion object {
        @Volatile
        private var instance: CaffeinateAccessibilityService? = null

        fun requestPulse(): Boolean {
            return instance?.performKeepAwakePulse() == true
        }
    }
}
