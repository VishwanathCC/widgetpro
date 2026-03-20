package com.vcc.widgetpro

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CaffeinateActivity : AppCompatActivity() {

    companion object {
        private const val ACTION_START = "com.vcc.widgetpro.ACTION_START_CAFFEINATE"
        private const val ACTION_STOP = "com.vcc.widgetpro.ACTION_STOP_CAFFEINATE"
        private const val EXTRA_MODE = "mode"

        fun createStartIntent(context: Context, mode: CaffeinateMode): Intent {
            return Intent(context, CaffeinateActivity::class.java).apply {
                action = ACTION_START
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_MODE, mode.name)
            }
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, CaffeinateActivity::class.java).apply {
                action = ACTION_STOP
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable {
        CaffeinateWidgetProvider.persistMode(this, CaffeinateMode.OFF)
        CaffeinateWidgetProvider.refreshWidgets(this)
        finish()
    }
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        statusText = TextView(this).apply {
            textSize = 22f
            setTextColor(Color.WHITE)
        }

        val layout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(
                statusText,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            )
        }

        setContentView(layout)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onDestroy() {
        handler.removeCallbacks(stopRunnable)
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_STOP) {
            finish()
            return
        }

        val mode = intent?.getStringExtra(EXTRA_MODE)
            ?.let { runCatching { CaffeinateMode.valueOf(it) }.getOrNull() }
            ?: CaffeinateMode.OFF

        if (mode == CaffeinateMode.OFF) {
            finish()
            return
        }

        statusText.text = mode.screenLabel
        handler.removeCallbacks(stopRunnable)

        if (mode.minutes > 0) {
            handler.postDelayed(stopRunnable, mode.minutes * 60 * 1000L)
        }
    }
}
