package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.core.exception.NotFoundScheduleException
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import com.dh.ondot.schedule.fixture.ScheduleFixture
import com.dh.ondot.schedule.infra.ScheduleQueryRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.BDDMockito.given
import com.dh.ondot.schedule.fixture.MockitoHelper.anyNonNull
import com.dh.ondot.schedule.fixture.MockitoHelper.eqNonNull
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("ScheduleQueryService 테스트")
class ScheduleQueryServiceTest {

    @Mock
    private lateinit var scheduleRepository: ScheduleRepository

    @Mock
    private lateinit var scheduleQueryRepository: ScheduleQueryRepository

    @InjectMocks
    private lateinit var scheduleQueryService: ScheduleQueryService

    @Test
    @DisplayName("ID로 스케줄을 조회한다")
    fun findScheduleById_ExistingId_ReturnsSchedule() {
        // given
        val scheduleId = 1L
        val schedule = ScheduleFixture.defaultSchedule()
        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule))

        // when
        val result = scheduleQueryService.findScheduleById(scheduleId)

        // then
        assertThat(result).isEqualTo(schedule)
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외를 발생시킨다")
    fun findScheduleById_NonExistingId_ThrowsException() {
        // given
        val scheduleId = 999L
        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.empty())

        // when & then
        assertThatThrownBy { scheduleQueryService.findScheduleById(scheduleId) }
            .isInstanceOf(NotFoundScheduleException::class.java)
    }

    @Test
    @DisplayName("Eager 로딩으로 스케줄을 조회한다")
    fun findScheduleByIdEager_ExistingId_ReturnsSchedule() {
        // given
        val scheduleId = 1L
        val schedule = ScheduleFixture.defaultSchedule()
        given(scheduleQueryRepository.findScheduleById(scheduleId)).willReturn(Optional.of(schedule))

        // when
        val result = scheduleQueryService.findScheduleByIdEager(scheduleId)

        // then
        assertThat(result).isEqualTo(schedule)
    }

    @Test
    @DisplayName("회원 ID와 스케줄 ID로 스케줄을 조회한다")
    fun findScheduleByMemberIdAndId_ExistingSchedule_ReturnsSchedule() {
        // given
        val memberId = 1L
        val scheduleId = 1L
        val schedule = ScheduleFixture.builder()
            .memberId(memberId)
            .build()

        given(scheduleQueryRepository.findScheduleByMemberIdAndId(memberId, scheduleId))
            .willReturn(Optional.of(schedule))

        // when
        val result = scheduleQueryService.findScheduleByMemberIdAndId(memberId, scheduleId)

        // then
        assertThat(result).isEqualTo(schedule)
        assertThat(result.memberId).isEqualTo(memberId)
    }

    @Test
    @DisplayName("다른 회원의 스케줄 조회 시 예외를 발생시킨다")
    fun findScheduleByMemberIdAndId_DifferentMember_ThrowsException() {
        // given
        val memberId = 1L
        val scheduleId = 1L

        given(scheduleQueryRepository.findScheduleByMemberIdAndId(memberId, scheduleId))
            .willReturn(Optional.empty())

        // when & then
        assertThatThrownBy { scheduleQueryService.findScheduleByMemberIdAndId(memberId, scheduleId) }
            .isInstanceOf(NotFoundScheduleException::class.java)
    }

    @Test
    @DisplayName("회원의 활성 스케줄 목록을 페이징으로 조회한다")
    fun getActiveSchedules_ValidMember_ReturnsPagedSchedules() {
        // given
        val memberId = 1L
        val pageable = PageRequest.of(0, 10)

        val schedules = listOf(
            ScheduleFixture.builder().memberId(memberId).build(),
            ScheduleFixture.builder().memberId(memberId).build()
        )
        val scheduleSlice = SliceImpl(schedules, pageable, true)

        given(scheduleQueryRepository.findActiveSchedulesByMember(eqNonNull(memberId), anyNonNull(), eqNonNull(pageable)))
            .willReturn(scheduleSlice)

        // when
        val result = scheduleQueryService.getActiveSchedules(memberId, pageable)

        // then
        assertThat(result.content).hasSize(2)
        assertThat(result.hasNext()).isTrue()
        assertThat(result.content).allMatch { schedule -> schedule.memberId == memberId }
    }

    @Test
    @DisplayName("활성 스케줄이 없는 회원의 경우 빈 페이지를 반환한다")
    fun getActiveSchedules_NoActiveSchedules_ReturnsEmptyPage() {
        // given
        val memberId = 1L
        val pageable = PageRequest.of(0, 10)
        val emptySlice = SliceImpl(listOf<Schedule>(), pageable, false)

        given(scheduleQueryRepository.findActiveSchedulesByMember(eqNonNull(memberId), anyNonNull(), eqNonNull(pageable)))
            .willReturn(emptySlice)

        // when
        val result = scheduleQueryService.getActiveSchedules(memberId, pageable)

        // then
        assertThat(result.content).isEmpty()
        assertThat(result.hasNext()).isFalse()
    }

    @Test
    @DisplayName("현재 시간 이후의 활성 스케줄만 조회한다")
    fun getActiveSchedules_FiltersExpiredSchedules_ReturnsOnlyFutureSchedules() {
        // given
        val memberId = 1L
        val pageable = PageRequest.of(0, 10)

        val futureSchedules = listOf(
            ScheduleFixture.builder().memberId(memberId).build()
        )
        val scheduleSlice = SliceImpl(futureSchedules, pageable, false)

        given(scheduleQueryRepository.findActiveSchedulesByMember(eqNonNull(memberId), anyNonNull(), eqNonNull(pageable)))
            .willReturn(scheduleSlice)

        // when
        val result = scheduleQueryService.getActiveSchedules(memberId, pageable)

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].memberId).isEqualTo(memberId)
    }
}
