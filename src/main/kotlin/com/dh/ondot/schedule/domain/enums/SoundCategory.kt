package com.dh.ondot.schedule.domain.enums

import com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SOUND_CATEGORY
import com.dh.ondot.core.exception.UnsupportedException
import java.util.Locale

enum class SoundCategory {
    BRIGHT_ENERGY,
    FAST_INTENSE;

    companion object {
        @JvmStatic
        fun from(category: String): SoundCategory =
            try {
                valueOf(category.uppercase(Locale.ENGLISH))
            } catch (e: IllegalArgumentException) {
                throw UnsupportedException(UNSUPPORTED_SOUND_CATEGORY, category)
            }
    }
}
