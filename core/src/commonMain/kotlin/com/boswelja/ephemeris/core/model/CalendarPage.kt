package com.boswelja.ephemeris.core.model

import kotlinx.datetime.LocalDate

/**
 * Contains information about how the calendar should display a page.
 * @param rows A list of [CalendarRow] for the calendar to display.
 */
public data class CalendarPage internal constructor(
    val rows: List<CalendarRow>
) {

    /**
     * Get the index of the given [date], if this page were to be flattened to a 1d array.
     * @return -1 if the date was not found on this page, else the index of the date.
     */
    public fun getFlatIndexOf(date: LocalDate): Int {
        var currIndex = 0
        rows.forEach { row ->
            row.days.forEach {
                if (it.date == date) {
                    return currIndex
                }
                currIndex += 1
            }
        }
        return -1
    }

    /**
     * Gets the [CalendarDay] at the given flat [index], or the index of the date if this page were
     * to be flattened to a 1d array. Throws [IllegalStateException] of the index does not exist.
     */
    public fun getDateForFlatIndex(index: Int): CalendarDay {
        var currIndex = 0
        rows.forEach { row ->
            row.days.forEach {
                if (currIndex == index) {
                    return it
                }
                currIndex += 1
            }
        }
        throw IllegalStateException("Index $index does not exist on this page")
    }
}
