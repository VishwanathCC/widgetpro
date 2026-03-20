package com.vcc.widgetpro

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews

class CaffeinateWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val state = CaffeinateStateStore(context).readState()
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, createViews(context, state))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_CYCLE) return

        val newState = CaffeinateStateStore(context).cycleState()
        CaffeinateSync.refreshWidgets(context)
        CaffeinateCommands.applyState(context)
    }

    private fun createViews(context: Context, state: CaffeinateState): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_caffeinate).apply {
            setTextViewText(R.id.widget_title, context.getString(R.string.widget_title))
            setTextViewText(R.id.widget_mode, state.mode.widgetLabel)
            setTextViewText(R.id.widget_hint, context.getString(R.string.widget_hint))
            setInt(
                R.id.widget_root,
                "setBackgroundColor",
                if (state.mode.isEnabled) 0xFF1E5631.toInt() else 0xFF202124.toInt()
            )
            setOnClickPendingIntent(R.id.widget_root, createTogglePendingIntent(context))
        }
    }

    private fun createTogglePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, CaffeinateWidgetProvider::class.java).apply {
            action = ACTION_CYCLE
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_WIDGET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
    }

    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    companion object {
        private const val ACTION_CYCLE = "com.vcc.widgetpro.action.CYCLE_FROM_WIDGET"
        private const val REQUEST_CODE_WIDGET = 3001
    }
}
