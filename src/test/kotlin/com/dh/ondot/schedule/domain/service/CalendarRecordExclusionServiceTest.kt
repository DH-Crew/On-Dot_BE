package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import com.dh.ondot.schedule.domain.repository.CalendarRecordExclusionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("CalendarRecordExclusionService 테스트")
class CalendarRecordExclusionServiceTest {

    @Mock
    private lateinit var repository: CalendarRecordExclusionRepository

    @InjectMocks
    private lateinit var service: CalendarRecordExclusionService

    @Test
    @DisplayName("기록 제외 시 저장한다")
    fun excludeRecord_Saves() {
        given(repository.existsByMemberIdAndScheduleIdAndExcludedDate(1L, 1L, LocalDate.of(2026, 3, 14)))
            .willReturn(false)
        given(repository.save(any(CalendarRecordExclusion::class.java)))
            .willAnswer { it.arguments[0] }

        service.excludeRecord(1L, 1L, LocalDate.of(2026, 3, 14))

        verify(repository).save(any(CalendarRecordExclusion::class.java))
    }

    @Test
    @DisplayName("이미 제외된 기록은 무시한다 (멱등성)")
    fun excludeRecord_AlreadyExists_Skips() {
        given(repository.existsByMemberIdAndScheduleIdAndExcludedDate(1L, 1L, LocalDate.of(2026, 3, 14)))
            .willReturn(true)

        service.excludeRecord(1L, 1L, LocalDate.of(2026, 3, 14))

        verify(repository, never()).save(any())
    }
}
