package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.core.exception.ErrorCode;
import com.dh.ondot.core.exception.UnsupportedException;

import java.util.Locale;

public enum Mission {
    NONE;

    public static Mission from(String mission) {
        try {
            return Mission.valueOf(mission.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_MISSION, mission);
        }
    }
}
