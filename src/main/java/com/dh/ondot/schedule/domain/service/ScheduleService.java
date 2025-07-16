package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.schedule.core.exception.NotFoundScheduleException;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    public Schedule setupSchedule(
            Member member, LocalDateTime appointmentAt, int estimatedTimeMin
    ) {
        Optional<Schedule> schedule = scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.getId());

        Schedule newSchedule;
        if (schedule.isPresent()) {
            newSchedule = createFromLatestUserSetting(schedule.get(), member, appointmentAt, estimatedTimeMin);
        } else {
            newSchedule = Schedule.createWithDefaultAlarmSetting(
                    member.getDefaultAlarmMode(), member.getSnooze(), member.getSound(),
                    appointmentAt, estimatedTimeMin, member.getPreparationTime()
            );
        }
        newSchedule.setupQuickSchedule(member.getId(), appointmentAt);

        return newSchedule;
    }

    private Schedule createFromLatestUserSetting(
            Schedule latestSchedule, Member member,
            LocalDateTime appointment, int estimatedTimeMin
    ) {
        Schedule copy = copySchedule(latestSchedule);

        LocalDateTime depAlarmAt = appointment.minusMinutes(estimatedTimeMin);
        LocalDateTime prepAlarmAt = depAlarmAt.minusMinutes(member.getPreparationTime());

        copy.getDepartureAlarm().updateTriggeredAt(depAlarmAt);
        copy.getPreparationAlarm().updateTriggeredAt(prepAlarmAt);

        return copy;
    }

    @Transactional
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public Schedule findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundScheduleException(id));
    }

    @Transactional
    public void deleteSchedule(Schedule schedule) {
        scheduleRepository.delete(schedule);
    }

    private Schedule copySchedule(Schedule original) {
        return Schedule.builder()
                .preparationAlarm(original.getPreparationAlarm().copy())
                .departureAlarm(original.getDepartureAlarm().copy())
                .build();
    }
}
