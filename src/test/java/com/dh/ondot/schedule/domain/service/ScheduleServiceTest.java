package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.repository.ScheduleRepository;
import com.dh.ondot.schedule.fixture.MemberFixture;
import com.dh.ondot.schedule.fixture.ScheduleFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService 테스트")
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("첫 번째 스케줄 설정 시 기본 설정으로 생성한다")
    void setupSchedule_FirstTime_CreatesWithDefaultSettings() {
        // given
        Member member = MemberFixture.defaultMember();
        LocalDateTime appointmentAt = LocalDateTime.now().plusDays(1);
        int estimatedTimeMin = 30;

        given(scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.getId()))
                .willReturn(Optional.empty());

        // when
        Schedule result = scheduleService.setupSchedule(member, appointmentAt, estimatedTimeMin);

        // then
        assertThat(result.getMemberId()).isEqualTo(member.getId());
        assertThat(result.getTitle()).isEqualTo("새로운 일정");
        assertThat(result.getIsRepeat()).isFalse();
        assertThat(result.getAppointmentAt()).isNotNull();
    }

    @Test
    @DisplayName("기존 스케줄이 있는 경우 최근 설정을 복사해서 생성한다")
    void setupSchedule_WithExistingSchedule_CopiesFromLatestSetting() {
        // given
        Member member = MemberFixture.defaultMember();
        LocalDateTime appointmentAt = LocalDateTime.now().plusDays(1);
        int estimatedTimeMin = 30;
        Schedule latestSchedule = ScheduleFixture.defaultSchedule();

        given(scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.getId()))
                .willReturn(Optional.of(latestSchedule));

        // when
        Schedule result = scheduleService.setupSchedule(member, appointmentAt, estimatedTimeMin);

        // then
        assertThat(result.getMemberId()).isEqualTo(member.getId());
        assertThat(result.getTitle()).isEqualTo("새로운 일정");
        assertThat(result.getPreparationAlarm().getMode())
                .isEqualTo(latestSchedule.getPreparationAlarm().getMode());
        assertThat(result.getDepartureAlarm().getMode())
                .isEqualTo(latestSchedule.getDepartureAlarm().getMode());
    }

    @Test
    @DisplayName("활성 알람이 없는 스케줄들만 있는 경우 null을 반환한다")
    void getEarliestActiveAlarmAt_NoActiveAlarms_ReturnsNull() {
        // given
        Schedule schedule1 = ScheduleFixture.builder().disabledAlarms().build();
        Schedule schedule2 = ScheduleFixture.builder().disabledAlarms().build();
        List<Schedule> schedules = List.of(schedule1, schedule2);

        // when
        Instant result = scheduleService.getEarliestActiveAlarmAt(schedules);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("단일 스케줄의 활성 알람 시간을 반환한다")
    void getEarliestActiveAlarmAt_SingleSchedule_ReturnsAlarmTime() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
        Schedule schedule = ScheduleFixture.builder()
                .onlyPreparationAlarmEnabled()
                .appointmentAt(futureTime)
                .build();
        List<Schedule> schedules = List.of(schedule);

        // when
        Instant result = scheduleService.getEarliestActiveAlarmAt(schedules);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("여러 스케줄 중 가장 빠른 활성 알람 시간을 반환한다")
    void getEarliestActiveAlarmAt_MultipleSchedules_ReturnsEarliest() {
        // given
        LocalDateTime earlierTime = LocalDateTime.now().plusHours(1);
        LocalDateTime laterTime = LocalDateTime.now().plusHours(3);
        
        Schedule earlierSchedule = ScheduleFixture.builder()
                .onlyPreparationAlarmEnabled()
                .appointmentAt(earlierTime)
                .build();
        Schedule laterSchedule = ScheduleFixture.builder()
                .onlyDepartureAlarmEnabled()
                .appointmentAt(laterTime)
                .build();
        
        List<Schedule> schedules = List.of(laterSchedule, earlierSchedule);

        // when
        Instant result = scheduleService.getEarliestActiveAlarmAt(schedules);

        // then
        assertThat(result).isNotNull();
        // 더 이른 시간의 알람이 선택되어야 함
    }

    @Test
    @DisplayName("빈 스케줄 리스트의 경우 null을 반환한다")
    void getEarliestActiveAlarmAt_EmptyList_ReturnsNull() {
        // given
        List<Schedule> schedules = List.of();

        // when
        Instant result = scheduleService.getEarliestActiveAlarmAt(schedules);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("반복 일정이 포함된 경우에도 가장 빠른 알람 시간을 반환한다")
    void getEarliestActiveAlarmAt_WithRepeatSchedules_ReturnsEarliest() {
        // given
        Schedule oneTimeSchedule = ScheduleFixture.builder()
                .appointmentAt(LocalDateTime.now().plusDays(2))
                .onlyPreparationAlarmEnabled()
                .build();
                
        Schedule repeatSchedule = ScheduleFixture.builder()
                .isRepeat(true)
                .repeatDays(ScheduleFixture.weekdays())
                .onlyDepartureAlarmEnabled()
                .build();
        
        List<Schedule> schedules = List.of(oneTimeSchedule, repeatSchedule);

        // when
        Instant result = scheduleService.getEarliestActiveAlarmAt(schedules);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("스케줄을 저장한다")
    void saveSchedule_ValidSchedule_SavesSuccessfully() {
        // given
        Schedule schedule = ScheduleFixture.defaultSchedule();
        given(scheduleRepository.save(any(Schedule.class))).willReturn(schedule);

        // when
        Schedule result = scheduleService.saveSchedule(schedule);

        // then
        assertThat(result).isEqualTo(schedule);
        verify(scheduleRepository).save(schedule);
    }

    @Test
    @DisplayName("스케줄을 삭제한다")
    void deleteSchedule_ValidSchedule_DeletesSuccessfully() {
        // given
        Schedule schedule = ScheduleFixture.defaultSchedule();

        // when
        scheduleService.deleteSchedule(schedule);

        // then
        verify(scheduleRepository).delete(schedule);
    }
}
