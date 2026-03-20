package com.vcc.widgetpro

object ScreenAwakePolicy {

    /*
     * What this app can realistically guarantee:
     * - The foreground service is explicit and user-visible.
     * - A PARTIAL_WAKE_LOCK keeps the process and timer logic alive more reliably.
     * - Widget, tile, and notification state stay synchronized from shared preferences.
     *
     * What it cannot guarantee:
     * - Third-party apps cannot force the display to remain lit across all apps on every device.
     * - FLAG_KEEP_SCREEN_ON only works for a visible window owned by this app, which this design
     *   intentionally avoids because the product has no full-screen UI.
     * - Some OEM builds still throttle or kill foreground services despite best practices.
     *
     * Optional production fallback, not implemented here:
     * - Accessibility-based interaction can simulate user activity on supported devices, but it
     *   raises privacy/review complexity and remains best-effort even when enabled by the user.
     */
    const val GUARANTEE_SUMMARY =
        "Foreground service + CPU wakelock improve continuity, but no public API guarantees display-on across all apps."
}
