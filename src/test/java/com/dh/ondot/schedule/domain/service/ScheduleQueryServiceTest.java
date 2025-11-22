package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.schedule.core.exception.NotFoundScheduleException;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.repository.ScheduleRepository;
import com.dh.ondot.schedule.fixture.ScheduleFixture;
import com.dh.ondot.schedule.infra.ScheduleQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleQueryService 테스트")
class ScheduleQueryServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleQueryRepository scheduleQueryRepository;

    @InjectMocks
    private ScheduleQueryService scheduleQueryService;

    @Test
    @DisplayName("ID로 스케줄을 조회한다")
    void findScheduleById_ExistingId_ReturnsSchedule() {
        // given
        Long scheduleId = 1L;
        Schedule schedule = ScheduleFixture.defaultSchedule();
        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

        // when
        Schedule result = scheduleQueryService.findScheduleById(scheduleId);

        // then
        assertThat(result).isEqualTo(schedule);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외를 발생시킨다")
    void findScheduleById_NonExistingId_ThrowsException() {
        // given
        Long scheduleId = 999L;
        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleQueryService.findScheduleById(scheduleId))
                .isInstanceOf(NotFoundScheduleException.class);
    }

    @Test
    @DisplayName("Eager 로딩으로 스케줄을 조회한다")
    void findScheduleByIdEager_ExistingId_ReturnsSchedule() {
        // given
        Long scheduleId = 1L;
        Schedule schedule = ScheduleFixture.defaultSchedule();
        given(scheduleQueryRepository.findScheduleById(scheduleId)).willReturn(Optional.of(schedule));

        // when
        Schedule result = scheduleQueryService.findScheduleByIdEager(scheduleId);

        // then
        assertThat(result).isEqualTo(schedule);
    }

    @Test
    @DisplayName("회원 ID와 스케줄 ID로 스케줄을 조회한다")
    void findScheduleByMemberIdAndId_ExistingSchedule_ReturnsSchedule() {
        // given
        Long memberId = 1L;
        Long scheduleId = 1L;
        Schedule schedule = ScheduleFixture.builder()
                .memberId(memberId)
                .build();
        
        given(scheduleQueryRepository.findScheduleByMemberIdAndId(memberId, scheduleId))
                .willReturn(Optional.of(schedule));

        // when
        Schedule result = scheduleQueryService.findScheduleByMemberIdAndId(memberId, scheduleId);

        // then
        assertThat(result).isEqualTo(schedule);
        assertThat(result.getMemberId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("다른 회원의 스케줄 조회 시 예외를 발생시킨다")
    void findScheduleByMemberIdAndId_DifferentMember_ThrowsException() {
        // given
        Long memberId = 1L;
        Long scheduleId = 1L;
        
        given(scheduleQueryRepository.findScheduleByMemberIdAndId(memberId, scheduleId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleQueryService.findScheduleByMemberIdAndId(memberId, scheduleId))
                .isInstanceOf(NotFoundScheduleException.class);
    }

    @Test
    @DisplayName("회원의 활성 스케줄 목록을 페이징으로 조회한다")
    void getActiveSchedules_ValidMember_ReturnsPagedSchedules() {
        // given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Schedule> schedules = List.of(
                ScheduleFixture.builder().memberId(memberId).build(),
                ScheduleFixture.builder().memberId(memberId).build()
        );
        Slice<Schedule> scheduleSlice = new SliceImpl<>(schedules, pageable, true);
        
        given(scheduleQueryRepository.findActiveSchedulesByMember(eq(memberId), any(Instant.class), eq(pageable)))
                .willReturn(scheduleSlice);

        // when
        Slice<Schedule> result = scheduleQueryService.getActiveSchedules(memberId, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getContent()).allMatch(schedule -> schedule.getMemberId().equals(memberId));
    }

    @Test
    @DisplayName("활성 스케줄이 없는 회원의 경우 빈 페이지를 반환한다")
    void getActiveSchedules_NoActiveSchedules_ReturnsEmptyPage() {
        // given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Slice<Schedule> emptySlice = new SliceImpl<>(List.of(), pageable, false);
        
        given(scheduleQueryRepository.findActiveSchedulesByMember(eq(memberId), any(Instant.class), eq(pageable)))
                .willReturn(emptySlice);

        // when
        Slice<Schedule> result = scheduleQueryService.getActiveSchedules(memberId, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("현재 시간 이후의 활성 스케줄만 조회한다")
    void getActiveSchedules_FiltersExpiredSchedules_ReturnsOnlyFutureSchedules() {
        // given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Schedule> futureSchedules = List.of(
                ScheduleFixture.builder().memberId(memberId).build()
        );
        Slice<Schedule> scheduleSlice = new SliceImpl<>(futureSchedules, pageable, false);
        
        given(scheduleQueryRepository.findActiveSchedulesByMember(eq(memberId), any(Instant.class), eq(pageable)))
                .willReturn(scheduleSlice);

        // when
        Slice<Schedule> result = scheduleQueryService.getActiveSchedules(memberId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMemberId()).isEqualTo(memberId);
    }
}
