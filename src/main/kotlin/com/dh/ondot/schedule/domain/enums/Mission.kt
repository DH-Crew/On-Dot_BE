package com.dh.ondot.schedule.domain.enums

import com.dh.ondot.core.exception.ErrorCode
import com.dh.ondot.core.exception.UnsupportedException
import java.util.Locale

enum class Mission {
    NONE;

    companion object {
        @JvmStatic
        fun from(mission: String): Mission =
            try {
                valueOf(mission.uppercase(Locale.ENGLISH))
            } catch (e: IllegalArgumentException) {
                throw UnsupportedException(ErrorCode.UNSUPPORTED_MISSION, mission)
            }
    }
}
