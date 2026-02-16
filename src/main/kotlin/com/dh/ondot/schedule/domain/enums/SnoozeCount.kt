package com.dh.ondot.schedule.domain.enums

import com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SNOOZE_COUNT
import com.dh.ondot.core.exception.UnsupportedException
import java.util.Locale

enum class SnoozeCount(val value: Int) {
    INFINITE(-1),
    ONE(1),
    THREE(3),
    FIVE(5),
    TEN(10);

    companion object {
        @JvmStatic
        fun from(count: String): SnoozeCount =
            try {
                valueOf(count.uppercase(Locale.ENGLISH))
            } catch (e: IllegalArgumentException) {
                throw UnsupportedException(UNSUPPORTED_SNOOZE_COUNT, count)
            }

        @JvmStatic
        fun from(value: Int): SnoozeCount =
            entries.firstOrNull { it.value == value }
                ?: throw UnsupportedException(UNSUPPORTED_SNOOZE_COUNT, value.toString())
    }
}
