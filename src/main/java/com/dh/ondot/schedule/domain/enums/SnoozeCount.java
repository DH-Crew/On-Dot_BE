package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.schedule.core.exception.UnsupportedException;
import lombok.Getter;

import java.util.Locale;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SNOOZE_COUNT;

@Getter
public enum SnoozeCount {
    INFINITE(-1),
    ONE(1),
    THREE(3),
    FIVE(5),
    TEN(10);

    private final int value;

    SnoozeCount(int value) {
        this.value = value;
    }

    public static SnoozeCount from(String count) {
        try {
            return SnoozeCount.valueOf(count.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(UNSUPPORTED_SNOOZE_COUNT, count);
        }
    }

    public static SnoozeCount from(int value) {
        for (SnoozeCount count : values()) {
            if (count.value == value) {
                return count;
            }
        }
        throw new UnsupportedException(UNSUPPORTED_SNOOZE_COUNT, String.valueOf(value));
    }
}
