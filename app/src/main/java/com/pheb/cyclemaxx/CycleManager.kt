package com.pheb.cyclemaxx

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

enum class MenstrualPhase {
    MENSTRUAL, FOLLICULAR, OVULATORY, LUTEAL
}

fun getPhaseFromStartDate(startDate: ZonedDateTime): MenstrualPhase {
    val today = ZonedDateTime.now(startDate.zone)
    val daysPassed = ChronoUnit.DAYS.between(startDate.toLocalDate(), today.toLocalDate()).toInt() + 1
    
    return when (daysPassed) {
        in 1..5 -> MenstrualPhase.MENSTRUAL
        in 6..12 -> MenstrualPhase.FOLLICULAR
        in 13..15 -> MenstrualPhase.OVULATORY
        in 16..28 -> MenstrualPhase.LUTEAL
        else -> MenstrualPhase.MENSTRUAL // Default for cycles > 28 days
    }
}

fun getPhaseFromTimestamp(timestamp: Long): MenstrualPhase {
    val startDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
    return getPhaseFromStartDate(startDate)
}

fun calculateCurrentPhase(daysSinceStart: Int): MenstrualPhase {
    return when (daysSinceStart) {
        in 1..5 -> MenstrualPhase.MENSTRUAL
        in 6..12 -> MenstrualPhase.FOLLICULAR
        in 13..15 -> MenstrualPhase.OVULATORY
        in 16..28 -> MenstrualPhase.LUTEAL
        else -> MenstrualPhase.MENSTRUAL
    }
}