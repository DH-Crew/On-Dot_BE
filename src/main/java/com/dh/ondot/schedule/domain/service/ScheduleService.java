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

    public Schedule createScheduleBasedOnMemberInfo(
            Member member,
            LocalDateTime appointmentAt,
            Integer estimatedTime
    ) {
        Optional<Schedule> schedule = scheduleRepository
                .findFirstByMemberIdOrderByUpdatedAtDesc(member.getId());

        if (schedule.isPresent()) {
            Schedule latestSchedule = copySchedule(schedule.get());
            latestSchedule.getPreparationAlarm().updateTriggeredAt(
                    appointmentAt.minusMinutes(estimatedTime + member.getPreparationTime())
            );
            latestSchedule.getDepartureAlarm().updateTriggeredAt(
                    appointmentAt.minusMinutes(estimatedTime)
            );
            latestSchedule.updateAppointmentAt(appointmentAt);

            return latestSchedule;
        } else {
            return Schedule.createWithDefaultAlarmSetting(
                    member.getDefaultAlarmMode(),
                    member.getSnooze(),
                    member.getSound(),
                    appointmentAt,
                    estimatedTime,
                    member.getPreparationTime()
            );
        }
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
