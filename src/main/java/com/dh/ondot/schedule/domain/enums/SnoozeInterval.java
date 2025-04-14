package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.core.exception.UnsupportedException;
import lombok.Getter;

import java.util.Locale;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SNOOZE_INTERVAL;

@Getter
public enum SnoozeInterval {
    ONE(1),
    THREE(3),
    FIVE(5),
    TEN(10),
    THIRTY(30),
    SIXTY(60);

    private final int value;

    SnoozeInterval(int value) {
        this.value = value;
    }

    public static SnoozeInterval from(String interval) {
        try {
            return SnoozeInterval.valueOf(interval.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(UNSUPPORTED_SNOOZE_INTERVAL, interval);
        }
    }

    public static SnoozeInterval from(int value) {
        for (SnoozeInterval interval : values()) {
            if (interval.value == value) {
                return interval;
            }
        }
        throw new UnsupportedException(UNSUPPORTED_SNOOZE_INTERVAL, String.valueOf(value));
    }
}
