package com.dh.ondot.core.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@UtilityClass
public class DateTimeUtils {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    public static LocalDateTime toSeoulDateTime(Instant instant) {
        return instant.atZone(DEFAULT_ZONE).toLocalDateTime();
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(DEFAULT_ZONE).toInstant();
    }

    public static LocalDateTime nowSeoulDateTime() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    public static LocalDate nowSeoulDate() {
        return LocalDate.now(DEFAULT_ZONE);
    }
}
