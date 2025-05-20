package com.dh.ondot.schedule.app;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.schedule.api.request.ScheduleCreateRequest;
import com.dh.ondot.schedule.api.request.ScheduleUpdateRequest;
import com.dh.ondot.schedule.api.request.VoiceScheduleCreateRequest;
import com.dh.ondot.schedule.app.dto.UpdateScheduleResult;
import com.dh.ondot.schedule.domain.Alarm;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.service.RouteService;
import com.dh.ondot.schedule.domain.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class ScheduleCommandFacade {
    private final MemberService memberService;
    private final ScheduleService scheduleService;
    private final RouteService routeService;

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
                request.preparationAlarm().triggeredAt(),
//                request.preparationAlarm().mission(),
                request.preparationAlarm().isSnoozeEnabled(),
                request.preparationAlarm().snoozeInterval(),
                request.preparationAlarm().snoozeCount(),
                request.preparationAlarm().soundCategory(),
                request.preparationAlarm().ringTone(),
                request.preparationAlarm().volume()
        );

        Alarm departureAlarm = Alarm.createDepartureAlarm(
                request.departureAlarm().alarmMode(),
                request.departureAlarm().triggeredAt(),
                request.departureAlarm().isSnoozeEnabled(),
                request.departureAlarm().snoozeInterval(),
                request.departureAlarm().snoozeCount(),
                request.departureAlarm().soundCategory(),
                request.departureAlarm().ringTone(),
                request.departureAlarm().volume()
        );

        Schedule schedule = Schedule.createSchedule(
                memberId,
                departurePlace,
                arrivalPlace,
                preparationAlarm,
                departureAlarm,
                request.title(),
                request.isRepeat(),
                new TreeSet<>(request.repeatDays()),
                request.appointmentAt()
        );

        return scheduleService.saveSchedule(schedule);
    }

    public void createVoiceSchedule(Long memberId, VoiceScheduleCreateRequest request) {
        Member member = memberService.findExistingMember(memberId);

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
        // todo: 시간 계산 후 알람 생성 비동기 처리 + 생성 알림

        Integer estimatedTime = routeService.calculateRouteTime(
                request.departurePlace().longitude(), request.departurePlace().latitude(),
                request.arrivalPlace().longitude(), request.arrivalPlace().latitude()
        );

        Schedule schedule = scheduleService.createScheduleBasedOnMemberInfo(
                member, request.appointmentAt(), estimatedTime
        );

        schedule.registerPlaces(departurePlace, arrivalPlace);

        scheduleService.saveSchedule(schedule);
    }

    @Transactional
    public UpdateScheduleResult updateSchedule(Long memberId, Long scheduleId, ScheduleUpdateRequest request) {
        memberService.findExistingMember(memberId);
        Schedule schedule = scheduleService.findScheduleById(scheduleId);

        // 장소 변경 여부 확인
        boolean departureChanged = schedule.getDeparturePlace().isPlaceChanged(
                request.departurePlace().roadAddress(),
                request.departurePlace().longitude(),
                request.departurePlace().latitude()
        );

        boolean arrivalChanged = schedule.getArrivalPlace().isPlaceChanged(
                request.arrivalPlace().roadAddress(),
                request.arrivalPlace().longitude(),
                request.arrivalPlace().latitude()
        );

        boolean placeChanged = departureChanged || arrivalChanged;
        boolean timeChanged = schedule.isAppointmentTimeChanged(request.appointmentAt());

        // 장소가 달라졌다면 → (비동기) 새로운 시간 계산 후 처리 (TODO 주석으로 남김)
        if (placeChanged || timeChanged) {
            // TODO: 경로 재계산·도착/출발 시간 보정 등의 비동기 로직 호출
        }

        schedule.getDeparturePlace().update(
                request.departurePlace().title(),
                request.departurePlace().roadAddress(),
                request.departurePlace().longitude(),
                request.departurePlace().latitude()
        );

        schedule.getArrivalPlace().update(
                request.arrivalPlace().title(),
                request.arrivalPlace().roadAddress(),
                request.arrivalPlace().longitude(),
                request.arrivalPlace().latitude()
        );

        schedule.getPreparationAlarm().updatePreparation(
                request.preparationAlarm().alarmMode(),
                request.preparationAlarm().isEnabled(),
                request.appointmentAt().toLocalDate()
                        .atTime(request.preparationAlarm().triggeredAt()),
//                request.preparationAlarm().mission(),
                request.preparationAlarm().isSnoozeEnabled(),
                request.preparationAlarm().snoozeInterval(),
                request.preparationAlarm().snoozeCount(),
                request.preparationAlarm().soundCategory(),
                request.preparationAlarm().ringTone(),
                request.preparationAlarm().volume()
        );

        schedule.getDepartureAlarm().updateDeparture(
                request.departureAlarm().alarmMode(),
                request.departureAlarm().isSnoozeEnabled(),
                request.departureAlarm().snoozeInterval(),
                request.departureAlarm().snoozeCount(),
                request.departureAlarm().soundCategory(),
                request.departureAlarm().ringTone(),
                request.departureAlarm().volume()
        );

        schedule.updateCore(
                request.title(),
                request.isRepeat(),
                new TreeSet<>(request.repeatDays()),
                request.appointmentAt()
        );
        // todo: nextAlarmAt 업데이트 로직 추가

        return new UpdateScheduleResult(schedule, placeChanged || timeChanged);
    }

    @Transactional
    public Schedule switchAlarm(
            Long memberId, Long scheduleId, boolean enabled
    ) {
        memberService.findExistingMember(memberId);

        Schedule schedule = scheduleService.findScheduleById(scheduleId);
        schedule.switchAlarm(enabled);

        return schedule;
    }

    public void deleteSchedule(Long memberId, Long scheduleId) {
        memberService.findExistingMember(memberId);
        Schedule schedule = scheduleService.findScheduleById(scheduleId);
        scheduleService.deleteSchedule(schedule);
    }
}
