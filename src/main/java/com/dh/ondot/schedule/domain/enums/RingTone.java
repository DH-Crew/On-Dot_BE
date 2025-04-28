package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.core.exception.ErrorCode;
import com.dh.ondot.core.exception.UnsupportedException;

import java.util.Locale;

public enum RingTone {
    // Bright Energy
    DANCING_IN_THE_STARDUST,
    IN_THE_CITY_LIGHTS_MIST,
    FRACTURED_LOVE,
    CHASING_LIGHTS,
    ASHES_OF_US,
    HEATING_SUN,
    NO_COPYRIGHT_MUSIC,

    // Fast & Intense
    MEDAL,
    EXCITING_SPORTS_COMPETITIONS,
    POSITIVE_WAY,
    ENERGETIC_HAPPY_UPBEAT_ROCK_MUSIC,
    ENERGY_CATCHER,
    ;

    public static RingTone from(String ringTone) {
        try {
            return RingTone.valueOf(ringTone.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_RING_TONE, ringTone);
        }
    }
}
