package com.dh.ondot.schedule.domain;

import com.dh.ondot.schedule.core.exception.UnsupportedSoundModeException;

import java.util.Locale;

public enum SoundMode {
    SILENT,
    VIBRATE,
    SOUND
    ;

    public static SoundMode from(String type) {
        try {
            return SoundMode.valueOf(type.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedSoundModeException(type);
        }
    }
}
