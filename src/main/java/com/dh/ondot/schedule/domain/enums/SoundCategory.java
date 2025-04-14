package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.core.exception.UnsupportedException;

import java.util.Locale;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SOUND_CATEGORY;

public enum SoundCategory {
    ;

    public static SoundCategory from(String category) {
        try {
            return SoundCategory.valueOf(category.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(UNSUPPORTED_SOUND_CATEGORY, category);
        }
    }
}
