package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.core.exception.ErrorCode;
import com.dh.ondot.core.exception.UnsupportedException;

import java.util.Locale;

public enum AlarmMode {
    SILENT,
    VIBRATE,
    SOUND;

    public static AlarmMode from(String mode) {
        try {
            return AlarmMode.valueOf(mode.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException (ErrorCode.UNSUPPORTED_ALARM_MODE, mode);
        }
    }
}
