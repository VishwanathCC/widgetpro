package com.vcc.widgetpro

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews

class CaffeinateWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE = "com.vcc.widgetpro.ACTION_TOGGLE"
        private const val PREFS_NAME = "widget_prefs"
        private const val PREF_MODE = "mode"

        fun currentMode(context: Context): CaffeinateMode {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return readMode(prefs.getString(PREF_MODE, CaffeinateMode.OFF.name))
        }

        fun persistMode(context: Context, mode: CaffeinateMode) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_MODE, mode.name)
                .apply()
        }

        fun refreshWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CaffeinateWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                CaffeinateWidgetProvider().updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun readMode(value: String?): CaffeinateMode {
            return value
                ?.let { runCatching { CaffeinateMode.valueOf(it) }.getOrNull() }
                ?: CaffeinateMode.OFF
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != ACTION_TOGGLE) {
            return
        }

        val currentMode = currentMode(context)
        val newMode = nextMode(currentMode)
        persistMode(context, newMode)

        if (newMode == CaffeinateMode.OFF) {
            context.startActivity(CaffeinateActivity.createStopIntent(context))
        } else {
            context.startActivity(CaffeinateActivity.createStartIntent(context, newMode))
        }

        refreshWidgets(context)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_caffeinate)
        val mode = currentMode(context)

        views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_title))
        views.setTextViewText(R.id.widget_mode, mode.widgetLabel)
        views.setTextViewText(R.id.widget_hint, context.getString(R.string.widget_hint))

        val toggleIntent = Intent(context, CaffeinateWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )

        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }
}
