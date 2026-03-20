package com.vcc.widgetpro

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val stateStore by lazy { CaffeinateStateStore(applicationContext) }
    private val overlaySettingsStore by lazy { OverlaySettingsStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.status_text)
        val detailText = findViewById<TextView>(R.id.detail_text)
        val overlayText = findViewById<TextView>(R.id.overlay_text)
        val cycleButton = findViewById<Button>(R.id.cycle_button)
        val batteryButton = findViewById<Button>(R.id.battery_button)
        val overlayToggleButton = findViewById<Button>(R.id.overlay_toggle_button)
        val overlayPermissionButton = findViewById<Button>(R.id.overlay_permission_button)

        fun render() {
            val state = stateStore.readState()
            statusText.text = getString(R.string.main_status_format, state.mode.widgetLabel)
            detailText.text = ScreenAwakePolicy.GUARANTEE_SUMMARY
            overlayText.text = buildOverlayStatus()
            overlayToggleButton.text = if (overlaySettingsStore.isOverlayFallbackEnabled()) {
                getString(R.string.main_overlay_disable_button)
            } else {
                getString(R.string.main_overlay_enable_button)
            }
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

        overlayToggleButton.setOnClickListener {
            val enabled = !overlaySettingsStore.isOverlayFallbackEnabled()
            overlaySettingsStore.setOverlayFallbackEnabled(enabled)
            if (enabled && !OverlayPermissionHelper.canDrawOverlays(this)) {
                startActivity(OverlayPermissionHelper.createManageOverlayIntent(this))
            } else {
                CaffeinateCommands.applyState(applicationContext)
            }
            render()
        }

        overlayPermissionButton.setOnClickListener {
            startActivity(OverlayPermissionHelper.createManageOverlayIntent(this))
        }

        render()
    }

    override fun onResume() {
        super.onResume()
        val statusText = findViewById<TextView>(R.id.status_text)
        val detailText = findViewById<TextView>(R.id.detail_text)
        val overlayText = findViewById<TextView>(R.id.overlay_text)
        val overlayToggleButton = findViewById<Button>(R.id.overlay_toggle_button)
        val state = stateStore.readState()
        statusText.text = getString(R.string.main_status_format, state.mode.widgetLabel)
        detailText.text = ScreenAwakePolicy.GUARANTEE_SUMMARY
        overlayText.text = buildOverlayStatus()
        overlayToggleButton.text = if (overlaySettingsStore.isOverlayFallbackEnabled()) {
            getString(R.string.main_overlay_disable_button)
        } else {
            getString(R.string.main_overlay_enable_button)
        }
        if (state.mode.isEnabled) {
            CaffeinateCommands.applyState(applicationContext)
        }
    }

    private fun buildOverlayStatus(): String {
        val enabled = overlaySettingsStore.isOverlayFallbackEnabled()
        val hasPermission = OverlayPermissionHelper.canDrawOverlays(this)
        return when {
            enabled && hasPermission -> getString(R.string.overlay_status_enabled)
            enabled -> getString(R.string.overlay_status_needs_permission)
            else -> getString(R.string.overlay_status_disabled)
        }
    }
}
