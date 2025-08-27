package com.dh.ondot.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DateTimeUtils 테스트")
class TimeUtilsTest {

    @Test
    @DisplayName("Instant를 서울 LocalDateTime으로 변환한다")
    void toSeoulDateTime_ConvertsInstantToLocalDateTime() {
        // given
        Instant instant = Instant.now();

        // when
        LocalDateTime result = TimeUtils.toSeoulDateTime(instant);

        // then
        assertThat(result).isNotNull();
        LocalDateTime expected = instant.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("LocalDateTime을 서울 시간 기준 Instant로 변환한다")
    void toInstant_ConvertsLocalDateTimeToInstant() {
        // given
        LocalDateTime localDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        // when
        Instant result = TimeUtils.toInstant(localDateTime);

        // then
        assertThat(result).isNotNull();
        Instant expected = localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Instant를 서울 시간 LocalTime으로 변환한다")
    void toSeoulTime_ConvertsInstantToLocalTime() {
        // given
        LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 15, 30, 45);
        Instant instant = TimeUtils.toInstant(testDateTime);

        // when
        LocalTime result = TimeUtils.toSeoulTime(instant);

        // then
        assertThat(result).isEqualTo(LocalTime.of(15, 30, 45));
    }

    @Test
    @DisplayName("현재 서울 시간을 LocalDateTime으로 반환한다")
    void nowSeoulDateTime_ReturnsCurrentSeoulTime() {
        // when
        LocalDateTime result = TimeUtils.nowSeoulDateTime();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1));
        assertThat(result).isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(1));
    }

    @Test
    @DisplayName("두 Instant 중 현재 시간 이후의 가장 빠른 시간을 찾는다")
    void findEarliestAfterNow_BothValid_ReturnsEarlier() {
        // given
        Instant now = Instant.now();
        Instant earlier = now.plusSeconds(3600); // 1시간 후
        Instant later = now.plusSeconds(7200);   // 2시간 후

        // when
        Instant result = TimeUtils.findEarliestAfterNow(later, earlier);

        // then
        assertThat(result).isEqualTo(earlier);
    }

    @Test
    @DisplayName("하나만 유효한 시간인 경우 유효한 시간을 반환한다")
    void findEarliestAfterNow_OneValid_ReturnsValid() {
        // given
        Instant now = Instant.now();
        Instant future = now.plusSeconds(3600);
        Instant past = now.minusSeconds(3600);

        // when
        Instant result = TimeUtils.findEarliestAfterNow(past, future);

        // then
        assertThat(result).isEqualTo(future);
    }

    @Test
    @DisplayName("둘 다 과거 시간이면 null을 반환한다")
    void findEarliestAfterNow_BothPast_ReturnsNull() {
        // given
        Instant now = Instant.now();
        Instant past1 = now.minusSeconds(3600);
        Instant past2 = now.minusSeconds(1800);

        // when
        Instant result = TimeUtils.findEarliestAfterNow(past1, past2);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("둘 다 null이면 null을 반환한다")
    void findEarliestAfterNow_BothNull_ReturnsNull() {
        // when
        Instant result = TimeUtils.findEarliestAfterNow(null, null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("하나가 null이고 다른 하나가 유효하면 유효한 시간을 반환한다")
    void findEarliestAfterNow_OneNullOneValid_ReturnsValid() {
        // given
        Instant future = Instant.now().plusSeconds(3600);

        // when
        Instant result1 = TimeUtils.findEarliestAfterNow(null, future);
        Instant result2 = TimeUtils.findEarliestAfterNow(future, null);

        // then
        assertThat(result1).isEqualTo(future);
        assertThat(result2).isEqualTo(future);
    }

    @Test
    @DisplayName("두 시간이 동일한 경우 해당 시간을 반환한다")
    void findEarliestAfterNow_SameTime_ReturnsSameTime() {
        // given
        Instant future = Instant.now().plusSeconds(3600);

        // when
        Instant result = TimeUtils.findEarliestAfterNow(future, future);

        // then
        assertThat(result).isEqualTo(future);
    }
}
