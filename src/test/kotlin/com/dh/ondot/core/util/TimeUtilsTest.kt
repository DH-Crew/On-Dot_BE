package com.dh.ondot.core.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@DisplayName("DateTimeUtils 테스트")
class TimeUtilsTest {

    @Test
    @DisplayName("Instant를 서울 LocalDateTime으로 변환한다")
    fun toSeoulDateTime_ConvertsInstantToLocalDateTime() {
        val instant = Instant.now()

        val result = TimeUtils.toSeoulDateTime(instant)

        assertThat(result).isNotNull
        val expected = instant.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("null Instant를 전달하면 null을 반환한다")
    fun toSeoulDateTime_NullInstant_ReturnsNull() {
        val result = TimeUtils.toSeoulDateTime(null)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("LocalDateTime을 서울 시간 기준 Instant로 변환한다")
    fun toInstant_ConvertsLocalDateTimeToInstant() {
        val localDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0)

        val result = TimeUtils.toInstant(localDateTime)

        assertThat(result).isNotNull
        val expected = localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("Instant를 서울 시간 LocalTime으로 변환한다")
    fun toSeoulTime_ConvertsInstantToLocalTime() {
        val testDateTime = LocalDateTime.of(2024, 1, 1, 15, 30, 45)
        val instant = TimeUtils.toInstant(testDateTime)

        val result = TimeUtils.toSeoulTime(instant)

        assertThat(result).isEqualTo(LocalTime.of(15, 30, 45))
    }

    @Test
    @DisplayName("null Instant를 전달하면 null LocalTime을 반환한다")
    fun toSeoulTime_NullInstant_ReturnsNull() {
        val result = TimeUtils.toSeoulTime(null)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("현재 서울 시간을 LocalDateTime으로 반환한다")
    fun nowSeoulDateTime_ReturnsCurrentSeoulTime() {
        val result = TimeUtils.nowSeoulDateTime()

        assertThat(result).isNotNull
        assertThat(result).isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1))
        assertThat(result).isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(1))
    }

    @Test
    @DisplayName("두 Instant 중 현재 시간 이후의 가장 빠른 시간을 찾는다")
    fun findEarliestAfterNow_BothValid_ReturnsEarlier() {
        val now = Instant.now()
        val earlier = now.plusSeconds(3600)
        val later = now.plusSeconds(7200)

        val result = TimeUtils.findEarliestAfterNow(later, earlier)

        assertThat(result).isEqualTo(earlier)
    }

    @Test
    @DisplayName("하나만 유효한 시간인 경우 유효한 시간을 반환한다")
    fun findEarliestAfterNow_OneValid_ReturnsValid() {
        val now = Instant.now()
        val future = now.plusSeconds(3600)
        val past = now.minusSeconds(3600)

        val result = TimeUtils.findEarliestAfterNow(past, future)

        assertThat(result).isEqualTo(future)
    }

    @Test
    @DisplayName("둘 다 과거 시간이면 null을 반환한다")
    fun findEarliestAfterNow_BothPast_ReturnsNull() {
        val now = Instant.now()
        val past1 = now.minusSeconds(3600)
        val past2 = now.minusSeconds(1800)

        val result = TimeUtils.findEarliestAfterNow(past1, past2)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("둘 다 null이면 null을 반환한다")
    fun findEarliestAfterNow_BothNull_ReturnsNull() {
        val result = TimeUtils.findEarliestAfterNow(null, null)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("하나가 null이고 다른 하나가 유효하면 유효한 시간을 반환한다")
    fun findEarliestAfterNow_OneNullOneValid_ReturnsValid() {
        val future = Instant.now().plusSeconds(3600)

        val result1 = TimeUtils.findEarliestAfterNow(null, future)
        val result2 = TimeUtils.findEarliestAfterNow(future, null)

        assertThat(result1).isEqualTo(future)
        assertThat(result2).isEqualTo(future)
    }

    @Test
    @DisplayName("두 시간이 동일한 경우 해당 시간을 반환한다")
    fun findEarliestAfterNow_SameTime_ReturnsSameTime() {
        val future = Instant.now().plusSeconds(3600)

        val result = TimeUtils.findEarliestAfterNow(future, future)

        assertThat(result).isEqualTo(future)
    }
}
