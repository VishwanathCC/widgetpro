package com.vcc.widgetpro

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val stateStore by lazy { CaffeinateStateStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.status_text)
        val detailText = findViewById<TextView>(R.id.detail_text)
        val accessibilityText = findViewById<TextView>(R.id.accessibility_text)
        val cycleButton = findViewById<Button>(R.id.cycle_button)
        val batteryButton = findViewById<Button>(R.id.battery_button)
        val accessibilityButton = findViewById<Button>(R.id.accessibility_button)

        fun render() {
            val state = stateStore.readState()
            statusText.text = getString(R.string.main_status_format, state.mode.widgetLabel)
            detailText.text = ScreenAwakePolicy.GUARANTEE_SUMMARY
            accessibilityText.text = AccessibilityFallbackHelper.describeAvailability(this)
        }

        cycleButton.setOnClickListener {
            stateStore.cycleState()
            CaffeinateCommands.applyState(applicationContext)
            CaffeinateSync.refreshSurfaces(applicationContext)
            render()
        }

        batteryButton.setOnClickListener {
            BatteryOptimizationHelper.createIgnoreBatteryOptimizationsIntent(this)?.let(::startActivity)
        }

        accessibilityButton.setOnClickListener {
            startActivity(AccessibilityFallbackHelper.createAccessibilitySettingsIntent())
        }

        render()
    }

    override fun onResume() {
        super.onResume()
        val statusText = findViewById<TextView>(R.id.status_text)
        val detailText = findViewById<TextView>(R.id.detail_text)
        val accessibilityText = findViewById<TextView>(R.id.accessibility_text)
        val state = stateStore.readState()
        statusText.text = getString(R.string.main_status_format, state.mode.widgetLabel)
        detailText.text = ScreenAwakePolicy.GUARANTEE_SUMMARY
        accessibilityText.text = AccessibilityFallbackHelper.describeAvailability(this)
    }
}
