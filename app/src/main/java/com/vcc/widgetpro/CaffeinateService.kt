package com.vcc.widgetpro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat

class CaffeinateService : Service() {

    private val stateStore by lazy { CaffeinateStateStore(applicationContext) }
    private val overlaySettingsStore by lazy { OverlaySettingsStore(applicationContext) }
    private val overlayController by lazy { KeepScreenOnOverlayController(applicationContext) }
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null

    private val timeoutRunnable = Runnable {
        val state = stateStore.readState()
        if (state.mode == CaffeinateMode.OFF || state.mode == CaffeinateMode.INFINITE) {
            return@Runnable
        }

        val remainingMs = (state.expiresAtElapsedRealtime ?: 0L) - SystemClock.elapsedRealtime()
        if (remainingMs <= 0L) {
            stopCaffeinate()
        } else {
            scheduleTimeout(remainingMs)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP || intent?.getBooleanExtra(EXTRA_STOP, false) == true) {
            stopCaffeinate()
            return START_STICKY
        }

        val storedState = stateStore.readState()
        if (CaffeinateCommands.isApplyState(intent) || intent == null) {
            applyStoredState(storedState)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        overlayController.hide()
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun applyStoredState(state: CaffeinateState) {
        if (!state.mode.isEnabled) {
            stopCaffeinate()
            return
        }

        val expiresAt = state.expiresAtElapsedRealtime ?: state.mode.minutes?.let { minutes ->
            SystemClock.elapsedRealtime() + minutes * 60_000L
        }
        stateStore.writeState(state.mode, expiresAt)

        if (state.mode != CaffeinateMode.INFINITE && expiresAt != null) {
            val remainingMs = expiresAt - SystemClock.elapsedRealtime()
            if (remainingMs <= 0L) {
                stopCaffeinate()
                return
            }
        }

        acquireWakeLockIfNeeded()
        startAsForeground(buildNotification(state.mode))

        timeoutHandler.removeCallbacks(timeoutRunnable)
        if (state.mode != CaffeinateMode.INFINITE && expiresAt != null) {
            scheduleTimeout(expiresAt - SystemClock.elapsedRealtime())
        }
        syncOverlayVisibility()

        // A foreground service plus a CPU wakelock is the most realistic baseline for a
        // utility app like Caffeine. It helps continuity across app switches, but Android no
        // longer offers a universal "force screen on everywhere" API for third-party apps.
        CaffeinateSync.refreshSurfaces(applicationContext)
    }

    private fun stopCaffeinate() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        overlayController.hide()
        stateStore.clear()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        CaffeinateSync.refreshSurfaces(applicationContext)
    }

    private fun acquireWakeLockIfNeeded() {
        if (wakeLock?.isHeld == true) return

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$packageName:CaffeinateCpuLock"
        )

        try {
            wakeLock?.acquire()
        } catch (t: Throwable) {
            releaseWakeLock()
            stopSelf()
            throw t
        }
    }

    private fun releaseWakeLock() {
        val lock = wakeLock ?: return
        try {
            if (lock.isHeld) {
                lock.release()
            }
        } finally {
            wakeLock = null
        }
    }

    private fun scheduleTimeout(delayMs: Long) {
        timeoutHandler.postDelayed(timeoutRunnable, delayMs.coerceAtLeast(1_000L))
    }

    private fun syncOverlayVisibility() {
        val shouldShowOverlay =
            overlaySettingsStore.isOverlayFallbackEnabled() &&
                OverlayPermissionHelper.canDrawOverlays(applicationContext)

        if (shouldShowOverlay) {
            overlayController.show()
        } else {
            overlayController.hide()
        }
    }

    private fun buildNotification(mode: CaffeinateMode): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_STOP,
            Intent(this, CaffeinateService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_STOP, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
        val batteryIntent = BatteryOptimizationHelper.createIgnoreBatteryOptimizationsIntent(this)
        val batteryPendingIntent = batteryIntent?.let {
            PendingIntent.getActivity(
                this,
                REQUEST_CODE_BATTERY,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            )
        }

        val contentText = buildString {
            append(mode.notificationLabel)
            append(". ")
            append(getString(R.string.notification_limitations))
            if (overlaySettingsStore.isOverlayFallbackEnabled()) {
                append(". ")
                append(getString(R.string.notification_overlay_enabled))
            }
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                NotificationCompat.Action(
                    0,
                    getString(R.string.notification_stop_action),
                    stopIntent
                )
            )

        if (batteryPendingIntent != null) {
            builder.addAction(
                NotificationCompat.Action(
                    0,
                    getString(R.string.notification_battery_action),
                    batteryPendingIntent
                )
            )
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun startAsForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    companion object {
        private const val CHANNEL_ID = "caffeinate_service"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE_STOP = 2001
        private const val REQUEST_CODE_BATTERY = 2002
        private const val ACTION_STOP = "com.vcc.widgetpro.action.STOP"
        private const val EXTRA_STOP = "extra_stop"

        fun start(context: Context, mode: CaffeinateMode) {
            context.applicationContext.let { appContext ->
                val expiresAt = mode.minutes?.let { SystemClock.elapsedRealtime() + it * 60_000L }
                CaffeinateStateStore(appContext).writeState(mode, expiresAt)
                CaffeinateCommands.applyState(appContext)
            }
        }

        fun stop(context: Context) {
            start(context, CaffeinateMode.OFF)
        }
    }
}
