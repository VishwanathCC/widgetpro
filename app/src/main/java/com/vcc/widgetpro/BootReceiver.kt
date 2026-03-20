package com.vcc.widgetpro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val stateStore = CaffeinateStateStore(context)
        val state = stateStore.readState()

        when (state.mode) {
            CaffeinateMode.INFINITE -> CaffeinateCommands.applyState(context)
            CaffeinateMode.OFF -> Unit
            else -> {
                // Timed sessions cannot be restored exactly after reboot because elapsed realtime
                // resets. Production apps often clear these and let the user re-enable manually.
                stateStore.clear()
                CaffeinateSync.refreshSurfaces(context)
            }
        }
    }
}
