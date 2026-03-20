package com.vcc.widgetpro

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class KeepScreenOnOverlayController(context: Context) {

    private val appContext = context.applicationContext
    private val windowManager = appContext.getSystemService(WindowManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private var pulseState = false

    private val pulseRunnable = object : Runnable {
        override fun run() {
            val view = overlayView ?: return
            pulseState = !pulseState
            view.alpha = if (pulseState) 0.08f else 0.03f
            mainHandler.postDelayed(this, PULSE_INTERVAL_MS)
        }
    }

    fun show() {
        if (overlayView != null || !OverlayPermissionHelper.canDrawOverlays(appContext)) return

        val sizePx = dp(12)
        val view = View(appContext).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
            alpha = 0.03f
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(4)
            y = dp(4)
        }

        windowManager.addView(view, params)
        overlayView = view
        pulseState = false
        mainHandler.post(pulseRunnable)
    }

    fun hide() {
        mainHandler.removeCallbacks(pulseRunnable)
        overlayView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        overlayView = null
    }

    private fun overlayWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            appContext.resources.displayMetrics
        ).toInt()
    }

    companion object {
        private const val PULSE_INTERVAL_MS = 5_000L
    }
}
