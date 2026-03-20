package com.vcc.widgetpro

enum class CaffeinateMode(
    val minutes: Int?,
    val widgetLabel: String,
    val notificationLabel: String,
    val tileSubtitle: String
) {
    OFF(
        minutes = null,
        widgetLabel = "OFF",
        notificationLabel = "Screen awake off",
        tileSubtitle = "Off"
    ),
    MINUTES_15(
        minutes = 15,
        widgetLabel = "15m",
        notificationLabel = "Keeping service active for 15 minutes",
        tileSubtitle = "15 min"
    ),
    MINUTES_30(
        minutes = 30,
        widgetLabel = "30m",
        notificationLabel = "Keeping service active for 30 minutes",
        tileSubtitle = "30 min"
    ),
    MINUTES_60(
        minutes = 60,
        widgetLabel = "60m",
        notificationLabel = "Keeping service active for 60 minutes",
        tileSubtitle = "60 min"
    ),
    INFINITE(
        minutes = null,
        widgetLabel = "INF",
        notificationLabel = "Keeping service active until turned off",
        tileSubtitle = "Infinite"
    );

    val isEnabled: Boolean
        get() = this != OFF

    fun next(): CaffeinateMode {
        val values = entries
        return values[(ordinal + 1) % values.size]
    }

    companion object {
        fun fromName(raw: String?): CaffeinateMode {
            return entries.firstOrNull { it.name == raw } ?: OFF
        }
    }
}
