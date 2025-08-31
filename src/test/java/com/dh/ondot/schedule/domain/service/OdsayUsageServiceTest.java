package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.core.exception.MaxOdsayUsageLimitExceededException;
import com.dh.ondot.schedule.domain.OdsayUsage;
import com.dh.ondot.schedule.domain.repository.OdsayUsageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OdsayUsageService 테스트")
class OdsayUsageServiceTest {

    @Mock
    private OdsayUsageRepository repository;

    @InjectMocks
    private OdsayUsageService odsayUsageService;

    @Test
    @DisplayName("오늘 첫 사용 시 새로운 사용량 레코드를 생성한다")
    void checkAndIncrementUsage_FirstUsageToday_CreatesNewRecord() {
        // given
        LocalDate today = LocalDate.of(2025, 8, 31);
        
        try (MockedStatic<TimeUtils> timeUtils = mockStatic(TimeUtils.class)) {
            timeUtils.when(TimeUtils::nowSeoulDate).thenReturn(today);
            
            given(repository.incrementUsageCount(today)).willReturn(0);
            given(repository.findByUsageDate(today)).willReturn(Optional.empty());

            // when
            odsayUsageService.checkAndIncrementUsage();

            // then
            verify(repository).incrementUsageCount(today);
            verify(repository).findByUsageDate(today);
            verify(repository).save(any(OdsayUsage.class));
        }
    }

    @Test
    @DisplayName("기존 사용량이 있고 한도 내일 때 원자성 업데이트로 증가한다")
    void checkAndIncrementUsage_ExistingUsageWithinLimit_UpdatesAtomically() {
        // given
        LocalDate today = LocalDate.of(2025, 8, 31);
        
        try (MockedStatic<TimeUtils> timeUtils = mockStatic(TimeUtils.class)) {
            timeUtils.when(TimeUtils::nowSeoulDate).thenReturn(today);
            
            given(repository.incrementUsageCount(today)).willReturn(1);

            // when
            odsayUsageService.checkAndIncrementUsage();

            // then
            verify(repository).incrementUsageCount(today);
            verify(repository, never()).findByUsageDate(any());
            verify(repository, never()).save(any());
        }
    }

    @Test
    @DisplayName("일일 사용량 한도 초과 시 예외를 발생시킨다")
    void checkAndIncrementUsage_UsageLimitExceeded_ThrowsException() {
        // given
        LocalDate today = LocalDate.of(2025, 8, 31);
        OdsayUsage limitExceededUsage = OdsayUsage.builder()
                .usageDate(today)
                .count(1000)
                .build();
        
        try (MockedStatic<TimeUtils> timeUtils = mockStatic(TimeUtils.class)) {
            timeUtils.when(TimeUtils::nowSeoulDate).thenReturn(today);
            
            given(repository.incrementUsageCount(today)).willReturn(0);
            given(repository.findByUsageDate(today)).willReturn(Optional.of(limitExceededUsage));

            // when & then
            assertThatThrownBy(() -> odsayUsageService.checkAndIncrementUsage())
                    .isInstanceOf(MaxOdsayUsageLimitExceededException.class);
            
            verify(repository).incrementUsageCount(today);
            verify(repository).findByUsageDate(today);
            verify(repository, never()).save(any());
        }
    }

    @Test
    @DisplayName("오늘 남은 사용 가능 횟수를 정확히 계산한다")
    void getRemainingUsageToday_WithExistingUsage_ReturnsCorrectCount() {
        // given
        LocalDate today = LocalDate.of(2025, 8, 31);
        int currentUsage = 150;
        int expectedRemaining = 1000 - 150;
        
        try (MockedStatic<TimeUtils> timeUtils = mockStatic(TimeUtils.class)) {
            timeUtils.when(TimeUtils::nowSeoulDate).thenReturn(today);
            
            given(repository.findUsageCountByDate(today)).willReturn(Optional.of(currentUsage));

            // when
            int remaining = odsayUsageService.getRemainingUsageToday();

            // then
            assertThat(remaining).isEqualTo(expectedRemaining);
            verify(repository).findUsageCountByDate(today);
        }
    }

    @Test
    @DisplayName("오늘 사용량이 없을 때 전체 한도를 반환한다")
    void getRemainingUsageToday_NoUsageToday_ReturnsFullLimit() {
        // given
        LocalDate today = LocalDate.of(2025, 8, 31);
        
        try (MockedStatic<TimeUtils> timeUtils = mockStatic(TimeUtils.class)) {
            timeUtils.when(TimeUtils::nowSeoulDate).thenReturn(today);
            
            given(repository.findUsageCountByDate(today)).willReturn(Optional.empty());

            // when
            int remaining = odsayUsageService.getRemainingUsageToday();

            // then
            assertThat(remaining).isEqualTo(1000);
            verify(repository).findUsageCountByDate(today);
        }
    }

    @Test
    @DisplayName("특정 날짜의 사용량을 조회한다")
    void getUsageCount_WithExistingDate_ReturnsUsageCount() {
        // given
        LocalDate targetDate = LocalDate.of(2025, 8, 30);
        int expectedCount = 250;
        
        given(repository.findUsageCountByDate(targetDate)).willReturn(Optional.of(expectedCount));

        // when
        int usageCount = odsayUsageService.getUsageCount(targetDate);

        // then
        assertThat(usageCount).isEqualTo(expectedCount);
        verify(repository).findUsageCountByDate(targetDate);
    }

    @Test
    @DisplayName("사용량이 없는 날짜 조회 시 0을 반환한다")
    void getUsageCount_WithNonExistingDate_ReturnsZero() {
        // given
        LocalDate targetDate = LocalDate.of(2025, 8, 29);
        
        given(repository.findUsageCountByDate(targetDate)).willReturn(Optional.empty());

        // when
        int usageCount = odsayUsageService.getUsageCount(targetDate);

        // then
        assertThat(usageCount).isEqualTo(0);
        verify(repository).findUsageCountByDate(targetDate);
    }
}
