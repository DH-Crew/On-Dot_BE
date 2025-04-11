package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.core.exception.ErrorCode;
import com.dh.ondot.schedule.core.exception.UnsupportedException;

import java.util.Locale;

public enum Mission {
    ;

    public static Mission from(String mission) {
        try {
            return Mission.valueOf(mission.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(ErrorCode.UNSUPPORTED_MISSION, mission);
        }
    }
}
