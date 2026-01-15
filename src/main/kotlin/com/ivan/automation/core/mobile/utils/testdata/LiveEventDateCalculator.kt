package com.ivan.automation.core.mobile.utils.testdata

import com.ivan.automation.core.mobile.api.models.enums.LiveEventState
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Calculator for live event date ranges based on event state.
 */
object LiveEventDateCalculator {

    private const val PAST_START_OFFSET_HOURS: Long = -1
    private const val PAST_END_OFFSET_HOURS: Long = -2
    private const val CURRENT_START_OFFSET_HOURS: Long = -1
    private const val CURRENT_END_OFFSET_HOURS: Long = 2
    private const val FUTURE_START_OFFSET_HOURS: Long = 2
    private const val FUTURE_END_OFFSET_HOURS: Long = 5

    /**
     * Computes a start/end date-time window (in ISO-8601 UTC format) based on the given live-event state.
     *
     * If "now" in UTC is 2025-04-23T12:00:00Z, then:
     * - for PAST    → returns Pair("2025-04-23T07:00:00Z", "2025-04-23T09:00:00Z")
     * - for CURRENT → returns Pair("2025-04-23T11:00:00Z", "2025-04-23T14:00:00Z")
     * - for FUTURE  → returns Pair("2025-04-23T15:00:00Z", "2025-04-23T17:00:00Z")
     *
     * @param eventState the state of the live event (PAST, CURRENT or FUTURE)
     * @return a Pair where
     *         - first = ISO-8601 UTC start timestamp
     *         - second = ISO-8601 UTC end timestamp
     */
    fun calculateDateRange(eventState: LiveEventState): Pair<String, String> {
        val nowUtc = OffsetDateTime.now(ZoneOffset.UTC)

        return when (eventState) {
            LiveEventState.PAST -> {
                formatDateTime(nowUtc, PAST_START_OFFSET_HOURS) to
                        formatDateTime(nowUtc, PAST_END_OFFSET_HOURS)
            }

            LiveEventState.CURRENT -> {
                formatDateTime(nowUtc, CURRENT_START_OFFSET_HOURS) to
                        formatDateTime(nowUtc, CURRENT_END_OFFSET_HOURS)
            }

            LiveEventState.FUTURE -> {
                formatDateTime(nowUtc, FUTURE_START_OFFSET_HOURS) to
                        formatDateTime(nowUtc, FUTURE_END_OFFSET_HOURS)
            }
        }
    }

    private fun formatDateTime(baseTime: OffsetDateTime, offsetHours: Long): String {
        return baseTime.plusHours(offsetHours).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
