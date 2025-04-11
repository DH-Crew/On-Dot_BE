package com.dh.ondot.schedule.app;

import com.dh.ondot.schedule.api.request.ScheduleCreateRequest;
import com.dh.ondot.schedule.api.request.VoiceScheduleCreateRequest;
import com.dh.ondot.schedule.domain.Alarm;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.SortedSet;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class ScheduleFacade {
    private final ScheduleService scheduleService;

    public Schedule createSchedule(Long memberId, ScheduleCreateRequest request) {
        Place departurePlace = Place.createPlace(
                request.departurePlace().title(),
                request.departurePlace().roadAddress(),
                request.departurePlace().longitude(),
                request.departurePlace().latitude()
        );

        Place arrivalPlace = Place.createPlace(
                request.arrivalPlace().title(),
                request.arrivalPlace().roadAddress(),
                request.arrivalPlace().longitude(),
                request.arrivalPlace().latitude()
        );

        Alarm preparationAlarm = Alarm.createPreparationAlarm(
                request.preparationAlarm().alarmMode(),
                request.preparationAlarm().isEnabled(),
                request.appointmentAt().toLocalDate().atTime(request.preparationAlarm().triggeredAt()),
                request.preparationAlarm().mission(),
                request.preparationAlarm().isSnoozeEnabled(),
                request.preparationAlarm().snoozeInterval(),
                request.preparationAlarm().snoozeCount(),
                request.preparationAlarm().soundCategory(),
                request.preparationAlarm().ringTone(),
                request.preparationAlarm().volume()
        );

        Alarm departureAlarm = Alarm.createDepartureAlarm(
                request.departureAlarm().alarmMode(),
                request.appointmentAt().toLocalDate().atTime(request.departureAlarm().triggeredAt()),
                request.departureAlarm().isSnoozeEnabled(),
                request.departureAlarm().snoozeInterval(),
                request.departureAlarm().snoozeCount(),
                request.departureAlarm().soundCategory(),
                request.departureAlarm().ringTone(),
                request.departureAlarm().volume()
        );

        SortedSet<Integer> repeatDays = request.isRepeat()
                ? new TreeSet<>(request.repeatDay())
                : null;

        Schedule schedule = Schedule.createSchedule(
                memberId,
                departurePlace,
                arrivalPlace,
                preparationAlarm,
                departureAlarm,
                request.title(),
                request.isRepeat(),
                repeatDays,
                request.appointmentAt()
        );

        return scheduleService.saveSchedule(schedule);
    }

    public void createVoiceSchedule(Long memberId, VoiceScheduleCreateRequest request) {
        Place departurePlace = Place.createPlace(
                request.departurePlace().title(),
                request.departurePlace().roadAddress(),
                request.departurePlace().longitude(),
                request.departurePlace().latitude()
        );

        Place arrivalPlace = Place.createPlace(
                request.arrivalPlace().title(),
                request.arrivalPlace().roadAddress(),
                request.arrivalPlace().longitude(),
                request.arrivalPlace().latitude()
        );
        // todo: 시간 계산 후 알람 생성 비동기 처리
    }
}
