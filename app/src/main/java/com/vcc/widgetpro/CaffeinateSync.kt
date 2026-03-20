package com.vcc.widgetpro

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.quicksettings.TileService

object CaffeinateSync {

    fun refreshSurfaces(context: Context) {
        refreshWidgets(context)
        requestTileRefresh()
    }

    fun refreshWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, CaffeinateWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (widgetIds.isNotEmpty()) {
            CaffeinateWidgetProvider().onUpdate(context, appWidgetManager, widgetIds)
        }
    }

    fun requestTileRefresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TileService.requestListeningState(
                App.instance,
                ComponentName(App.instance, CaffeinateTileService::class.java)
            )
        }
    }
}
