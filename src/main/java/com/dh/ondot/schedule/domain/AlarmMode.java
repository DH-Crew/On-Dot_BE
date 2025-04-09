package com.dh.ondot.schedule.domain;

import com.dh.ondot.schedule.core.exception.UnsupportedSoundModeException;

import java.util.Locale;

public enum AlarmMode {
    SILENT,
    VIBRATE,
    SOUND
    ;

    public static AlarmMode from(String type) {
        try {
            return AlarmMode.valueOf(type.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedSoundModeException(type);
        }
    }
}
