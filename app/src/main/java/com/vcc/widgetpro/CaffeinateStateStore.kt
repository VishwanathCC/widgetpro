package com.vcc.widgetpro

import android.content.Context
import android.content.SharedPreferences

class CaffeinateStateStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun readState(): CaffeinateState {
        return CaffeinateState(
            mode = CaffeinateMode.fromName(prefs.getString(KEY_MODE, CaffeinateMode.OFF.name)),
            expiresAtElapsedRealtime = prefs.getLong(KEY_EXPIRES_AT, NO_EXPIRATION).takeIf {
                it != NO_EXPIRATION
            }
        )
    }

    @Synchronized
    fun cycleState(): CaffeinateState {
        val nextMode = readState().mode.next()
        return writeState(nextMode)
    }

    @Synchronized
    fun writeState(mode: CaffeinateMode, expiresAtElapsedRealtime: Long? = null): CaffeinateState {
        prefs.edit()
            .putString(KEY_MODE, mode.name)
            .putLong(KEY_EXPIRES_AT, expiresAtElapsedRealtime ?: NO_EXPIRATION)
            .apply()
        return CaffeinateState(mode = mode, expiresAtElapsedRealtime = expiresAtElapsedRealtime)
    }

    @Synchronized
    fun clear(): CaffeinateState = writeState(CaffeinateMode.OFF)

    companion object {
        private const val PREFS_NAME = "caffeinate_state"
        private const val KEY_MODE = "mode"
        private const val KEY_EXPIRES_AT = "expires_at_elapsed"
        private const val NO_EXPIRATION = -1L
    }
}

data class CaffeinateState(
    val mode: CaffeinateMode,
    val expiresAtElapsedRealtime: Long?
)
