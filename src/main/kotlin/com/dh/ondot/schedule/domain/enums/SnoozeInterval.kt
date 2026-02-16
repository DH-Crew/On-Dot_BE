package com.dh.ondot.schedule.domain.enums

import com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SNOOZE_INTERVAL
import com.dh.ondot.core.exception.UnsupportedException
import java.util.Locale

enum class SnoozeInterval(val value: Int) {
    ONE(1),
    THREE(3),
    FIVE(5),
    TEN(10),
    THIRTY(30),
    SIXTY(60);

    companion object {
        @JvmStatic
        fun from(interval: String): SnoozeInterval =
            try {
                valueOf(interval.uppercase(Locale.ENGLISH))
            } catch (e: IllegalArgumentException) {
                throw UnsupportedException(UNSUPPORTED_SNOOZE_INTERVAL, interval)
            }

        @JvmStatic
        fun from(value: Int): SnoozeInterval =
            entries.firstOrNull { it.value == value }
                ?: throw UnsupportedException(UNSUPPORTED_SNOOZE_INTERVAL, value.toString())
    }
}
