package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.core.exception.ErrorCode;
import com.dh.ondot.schedule.core.exception.UnsupportedException;

import java.util.Locale;

public enum RingTone {
    ;

    public static RingTone from(String ringTone) {
        try {
            return RingTone.valueOf(ringTone.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_RING_TONE, ringTone);
        }
    }
}
