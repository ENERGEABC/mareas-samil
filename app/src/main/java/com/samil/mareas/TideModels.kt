package com.samil.mareas

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TideEvent(
    val dateTime: LocalDateTime,
    val heightMeters: Double,
    val isHighTide: Boolean
) {
    companion object {
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        fun parseDateTime(raw: String): LocalDateTime = LocalDateTime.parse(raw, FORMATTER)
    }
}

data class TideState(
    val previousEvent: TideEvent,
    val nextEvent: TideEvent,
    val progressFraction: Double,
    val rising: Boolean
)
