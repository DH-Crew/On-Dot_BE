package com.dh.ondot.core.util

import java.time.*

object TimeUtils {
    private val DEFAULT_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

    @JvmStatic
    fun toSeoulDateTime(instant: Instant?): LocalDateTime? {
        return instant?.atZone(DEFAULT_ZONE)?.toLocalDateTime()
    }

    @JvmStatic
    fun toInstant(localDateTime: LocalDateTime): Instant {
        return localDateTime.atZone(DEFAULT_ZONE).toInstant()
    }

    @JvmStatic
    fun nowSeoulDateTime(): LocalDateTime {
        return LocalDateTime.now(DEFAULT_ZONE)
    }

    @JvmStatic
    fun nowSeoulDate(): LocalDate {
        return LocalDate.now(DEFAULT_ZONE)
    }

    @JvmStatic
    fun nowSeoulInstant(): Instant {
        return ZonedDateTime.now(DEFAULT_ZONE).toInstant()
    }

    @JvmStatic
    fun toSeoulTime(instant: Instant?): LocalTime? {
        return instant?.atZone(DEFAULT_ZONE)?.toLocalTime()
    }

    @JvmStatic
    fun findEarliestAfterNow(time1: Instant?, time2: Instant?): Instant? {
        val now = Instant.now()
        val isTime1Valid = time1 != null && time1.isAfter(now)
        val isTime2Valid = time2 != null && time2.isAfter(now)

        return when {
            !isTime1Valid && !isTime2Valid -> null
            !isTime1Valid -> time2
            !isTime2Valid -> time1
            else -> if (time1!!.isBefore(time2!!)) time1 else time2
        }
    }
}
