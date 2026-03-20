package com.vcc.widgetpro

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager

object AccessibilityFallbackHelper {

    fun isServiceEnabled(context: Context): Boolean {
        val expectedComponent = ComponentName(context, CaffeinateAccessibilityService::class.java)
            .flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':').apply {
            setString(enabledServices)
        }
        while (splitter.hasNext()) {
            if (splitter.next().equals(expectedComponent, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun isAccessibilityGloballyEnabled(context: Context): Boolean {
        val manager = context.getSystemService(AccessibilityManager::class.java)
        return manager.isEnabled
    }

    fun createAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun describeAvailability(context: Context): String {
        return when {
            isServiceEnabled(context) -> context.getString(R.string.accessibility_status_enabled)
            isAccessibilityGloballyEnabled(context) -> context.getString(R.string.accessibility_status_needs_service)
            else -> context.getString(R.string.accessibility_status_disabled)
        }
    }

    fun buildServiceInfo(): AccessibilityServiceInfo {
        return AccessibilityServiceInfo().apply {
            eventTypes = 0
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 0
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
    }
}
