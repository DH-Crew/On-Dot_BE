package com.dh.ondot.schedule.domain.enums

import com.dh.ondot.core.exception.ErrorCode
import com.dh.ondot.core.exception.UnsupportedException
import java.util.Locale

enum class AlarmMode {
    SILENT,
    VIBRATE,
    SOUND;

    companion object {
        @JvmStatic
        fun from(mode: String): AlarmMode =
            try {
                valueOf(mode.uppercase(Locale.ENGLISH))
            } catch (e: IllegalArgumentException) {
                throw UnsupportedException(ErrorCode.UNSUPPORTED_ALARM_MODE, mode)
            }
    }
}
