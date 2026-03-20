package com.vcc.widgetpro

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class CaffeinateTileService : TileService() {

    private val stateStore by lazy { CaffeinateStateStore(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        updateTile(stateStore.readState())
    }

    override fun onClick() {
        super.onClick()
        val newState = stateStore.cycleState()
        updateTile(newState)
        CaffeinateCommands.applyState(applicationContext)
        CaffeinateSync.refreshWidgets(applicationContext)
    }

    private fun updateTile(state: CaffeinateState) {
        val tile = qsTile ?: return
        tile.label = getString(R.string.tile_label)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = state.mode.tileSubtitle
        }
        tile.state = if (state.mode.isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
