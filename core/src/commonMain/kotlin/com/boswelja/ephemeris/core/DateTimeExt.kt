package com.boswelja.ephemeris.core

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

private val monthCount = Month.values().size
private val dayOfWeekCount = DayOfWeek.values().size

internal fun Month.plusMonths(months: Long): Month {
    val newNumber = (this.number + months) % monthCount
    return if (newNumber > 0) {
        Month(newNumber.toInt())
    } else {
        Month(newNumber.toInt() + monthCount)
    }
}

internal fun DayOfWeek.minusDays(days: Long): DayOfWeek {
    val newNumber = (this.isoDayNumber - days) % dayOfWeekCount
    return if (newNumber > 0) {
        DayOfWeek(newNumber.toInt())
    } else {
        DayOfWeek(newNumber.toInt() + dayOfWeekCount)
    }
}

internal val DayOfWeek.value: Int
    get() = ordinal + 1
