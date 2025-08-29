package com.dh.ondot.core.util;

import lombok.experimental.UtilityClass;

import java.time.*;

@UtilityClass
public class TimeUtils {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    public static LocalDateTime toSeoulDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
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

    public static Instant nowSeoulInstant() {
        return ZonedDateTime.now(DEFAULT_ZONE).toInstant();
    }

    public static LocalTime toSeoulTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(DEFAULT_ZONE).toLocalTime();
    }

    /**
     * 두 Instant 중 현재 시간 이후이면서 더 빠른 시간을 반환한다.
     * 둘 다 현재 시간 이전이거나 null이면 null을 반환한다.
     */
    public static Instant findEarliestAfterNow(Instant time1, Instant time2) {
        Instant now = Instant.now();
        
        boolean isTime1Valid = time1 != null && time1.isAfter(now);
        boolean isTime2Valid = time2 != null && time2.isAfter(now);
        
        if (!isTime1Valid && !isTime2Valid) return null;
        if (!isTime1Valid) return time2;
        if (!isTime2Valid) return time1;
        
        return time1.isBefore(time2) ? time1 : time2;
    }
}
