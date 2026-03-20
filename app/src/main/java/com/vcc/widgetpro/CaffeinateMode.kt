package com.vcc.widgetpro

enum class CaffeinateMode(
    val minutes: Int,
    val widgetLabel: String,
    val screenLabel: String
) {
    MIN_15(15, "15", "Awake for 15 minutes"),
    MIN_30(30, "30", "Awake for 30 minutes"),
    MIN_60(60, "60", "Awake for 60 minutes"),
    INFINITE(-1, "Infinite", "Awake until turned off"),
    OFF(0, "Off", "Screen awake is off")
}

fun nextMode(current: CaffeinateMode): CaffeinateMode {
    return when (current) {
        CaffeinateMode.OFF -> CaffeinateMode.MIN_15
        CaffeinateMode.MIN_15 -> CaffeinateMode.MIN_30
        CaffeinateMode.MIN_30 -> CaffeinateMode.MIN_60
        CaffeinateMode.MIN_60 -> CaffeinateMode.INFINITE
        CaffeinateMode.INFINITE -> CaffeinateMode.OFF
    }
}
