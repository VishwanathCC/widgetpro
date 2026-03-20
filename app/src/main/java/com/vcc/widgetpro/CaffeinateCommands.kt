package com.vcc.widgetpro

import android.content.Context
import android.content.Intent

object CaffeinateCommands {
    private const val ACTION_APPLY_STATE = "com.vcc.widgetpro.action.APPLY_STATE"

    fun applyState(context: Context) {
        val intent = Intent(context, CaffeinateService::class.java)
            .setAction(ACTION_APPLY_STATE)

        context.startForegroundService(intent)
    }

    fun isApplyState(intent: Intent?): Boolean {
        return intent?.action == ACTION_APPLY_STATE
    }
}
