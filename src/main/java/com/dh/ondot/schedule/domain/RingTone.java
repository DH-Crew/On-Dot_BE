package com.dh.ondot.schedule.domain;

import com.dh.ondot.schedule.core.exception.UnsupportedRingToneException;

import java.util.Locale;

public enum RingTone {
    ;

    public static RingTone from(String ringTone) {
        try {
            return RingTone.valueOf(ringTone.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedRingToneException(ringTone);
        }
    }
}

