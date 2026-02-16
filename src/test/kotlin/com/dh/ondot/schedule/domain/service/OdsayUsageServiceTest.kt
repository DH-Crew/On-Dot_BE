package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.core.exception.MaxOdsayUsageLimitExceededException
import com.dh.ondot.schedule.domain.OdsayUsage
import com.dh.ondot.schedule.domain.repository.OdsayUsageRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import com.dh.ondot.schedule.fixture.MockitoHelper.anyNonNull
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("OdsayUsageService 테스트")
class OdsayUsageServiceTest {

    @Mock
    private lateinit var repository: OdsayUsageRepository

    @InjectMocks
    private lateinit var odsayUsageService: OdsayUsageService

    @Test
    @DisplayName("오늘 첫 사용 시 새로운 사용량 레코드를 생성한다")
    fun checkAndIncrementUsage_FirstUsageToday_CreatesNewRecord() {
        // given
        val today = LocalDate.of(2025, 8, 31)

        mockStatic(TimeUtils::class.java).use { timeUtils ->
            timeUtils.`when`<LocalDate> { TimeUtils.nowSeoulDate() }.thenReturn(today)

            given(repository.incrementUsageCount(today)).willReturn(0)

            // when
            odsayUsageService.checkAndIncrementUsage()

            // then
            verify(repository).incrementUsageCount(today)
            verify(repository).save(anyNonNull<OdsayUsage>())
        }
    }

    @Test
    @DisplayName("기존 사용량이 있고 한도 내일 때 원자성 업데이트로 증가한다")
    fun checkAndIncrementUsage_ExistingUsageWithinLimit_UpdatesAtomically() {
        // given
        val today = LocalDate.of(2025, 8, 31)

        mockStatic(TimeUtils::class.java).use { timeUtils ->
            timeUtils.`when`<LocalDate> { TimeUtils.nowSeoulDate() }.thenReturn(today)

            given(repository.incrementUsageCount(today)).willReturn(1)

            // when
            odsayUsageService.checkAndIncrementUsage()

            // then
            verify(repository).incrementUsageCount(today)
            verify(repository, never()).findByUsageDate(anyNonNull())
            verify(repository, never()).save(anyNonNull())
        }
    }

    @Test
    @DisplayName("일일 사용량 한도 초과 시 예외를 발생시킨다")
    fun checkAndIncrementUsage_UsageLimitExceeded_ThrowsException() {
        // given
        val today = LocalDate.of(2025, 8, 31)

        mockStatic(TimeUtils::class.java).use { timeUtils ->
            timeUtils.`when`<LocalDate> { TimeUtils.nowSeoulDate() }.thenReturn(today)

            given(repository.incrementUsageCount(today))
                .willReturn(0)  // First call
                .willReturn(0)  // Second call (retry after exception)

            doThrow(DataIntegrityViolationException::class.java)
                .`when`(repository).save(anyNonNull<OdsayUsage>())

            // when & then
            assertThatThrownBy { odsayUsageService.checkAndIncrementUsage() }
                .isInstanceOf(MaxOdsayUsageLimitExceededException::class.java)

            verify(repository, times(2)).incrementUsageCount(today)
            verify(repository).save(anyNonNull<OdsayUsage>())
        }
    }

    @Test
    @DisplayName("오늘 남은 사용 가능 횟수를 정확히 계산한다")
    fun getRemainingUsageToday_WithExistingUsage_ReturnsCorrectCount() {
        // given
        val today = LocalDate.of(2025, 8, 31)
        val currentUsage = 150
        val expectedRemaining = 1000 - 150

        mockStatic(TimeUtils::class.java).use { timeUtils ->
            timeUtils.`when`<LocalDate> { TimeUtils.nowSeoulDate() }.thenReturn(today)

            given(repository.findUsageCountByDate(today)).willReturn(Optional.of(currentUsage))

            // when
            val remaining = odsayUsageService.getRemainingUsageToday()

            // then
            assertThat(remaining).isEqualTo(expectedRemaining)
            verify(repository).findUsageCountByDate(today)
        }
    }

    @Test
    @DisplayName("오늘 사용량이 없을 때 전체 한도를 반환한다")
    fun getRemainingUsageToday_NoUsageToday_ReturnsFullLimit() {
        // given
        val today = LocalDate.of(2025, 8, 31)

        mockStatic(TimeUtils::class.java).use { timeUtils ->
            timeUtils.`when`<LocalDate> { TimeUtils.nowSeoulDate() }.thenReturn(today)

            given(repository.findUsageCountByDate(today)).willReturn(Optional.empty())

            // when
            val remaining = odsayUsageService.getRemainingUsageToday()

            // then
            assertThat(remaining).isEqualTo(1000)
            verify(repository).findUsageCountByDate(today)
        }
    }

    @Test
    @DisplayName("특정 날짜의 사용량을 조회한다")
    fun getUsageCount_WithExistingDate_ReturnsUsageCount() {
        // given
        val targetDate = LocalDate.of(2025, 8, 30)
        val expectedCount = 250

        given(repository.findUsageCountByDate(targetDate)).willReturn(Optional.of(expectedCount))

        // when
        val usageCount = odsayUsageService.getUsageCount(targetDate)

        // then
        assertThat(usageCount).isEqualTo(expectedCount)
        verify(repository).findUsageCountByDate(targetDate)
    }

    @Test
    @DisplayName("사용량이 없는 날짜 조회 시 0을 반환한다")
    fun getUsageCount_WithNonExistingDate_ReturnsZero() {
        // given
        val targetDate = LocalDate.of(2025, 8, 29)

        given(repository.findUsageCountByDate(targetDate)).willReturn(Optional.empty())

        // when
        val usageCount = odsayUsageService.getUsageCount(targetDate)

        // then
        assertThat(usageCount).isEqualTo(0)
        verify(repository).findUsageCountByDate(targetDate)
    }
}
