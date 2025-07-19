package com.dh.ondot.schedule.application;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.schedule.api.request.ScheduleCreateRequest;
import com.dh.ondot.schedule.api.request.ScheduleUpdateRequest;
import com.dh.ondot.schedule.api.request.QuickScheduleCreateRequest;
import com.dh.ondot.schedule.api.response.ScheduleParsedResponse;
import com.dh.ondot.schedule.application.command.QuickScheduleCommand;
import com.dh.ondot.schedule.application.dto.UpdateScheduleResult;
import com.dh.ondot.schedule.application.mapper.QuickScheduleMapper;
import com.dh.ondot.schedule.domain.Alarm;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent;
import com.dh.ondot.schedule.domain.service.AiUsageService;
import com.dh.ondot.schedule.domain.service.PlaceService;
import com.dh.ondot.schedule.domain.service.RouteService;
import com.dh.ondot.schedule.domain.service.ScheduleService;
import com.dh.ondot.schedule.infra.api.OpenAiPromptApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class ScheduleCommandFacade {
    private final MemberService memberService;
    private final ScheduleService scheduleService;
    private final RouteService routeService;
    private final PlaceService placeService;
    private final AiUsageService aiUsageService;
    private final QuickScheduleMapper quickScheduleMapper;
    private final OpenAiPromptApi openAiPromptApi;
    private final ApplicationEventPublisher eventPublisher;

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

    @Transactional
    public void createQuickSchedule(Long memberId, QuickScheduleCreateRequest request) {
        Member member = memberService.findExistingMember(memberId);

        Place dep = Place.createPlace(
                request.departurePlace().title(),
                request.departurePlace().roadAddress(),
                request.departurePlace().longitude(),
                request.departurePlace().latitude()
        );
        Place arr = Place.createPlace(
                request.arrivalPlace().title(),
                request.arrivalPlace().roadAddress(),
                request.arrivalPlace().longitude(),
                request.arrivalPlace().latitude()
        );

        int estimatedTime = routeService.calculateRouteTime(
                request.departurePlace().longitude(), request.departurePlace().latitude(),
                request.arrivalPlace().longitude(), request.arrivalPlace().latitude()
        );

        Schedule schedule = scheduleService.setupSchedule(
                member, request.appointmentAt(), estimatedTime
        );
        schedule.registerPlaces(dep, arr);

        scheduleService.saveSchedule(schedule);
    }

    @Transactional
    public void createQuickScheduleV1(Long memberId, QuickScheduleCreateRequest request) {
        memberService.findExistingMember(memberId);
        QuickScheduleCommand cmd = quickScheduleMapper.toCommand(memberId, request);
        QuickScheduleRequestedEvent event = placeService.savePlaces(cmd);
        eventPublisher.publishEvent(event);
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
                request.appointmentAt().toLocalDate()
                        .atTime(request.departureAlarm().triggeredAt()),
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
    public ScheduleParsedResponse parseVoiceSchedule(Long memberId, String sentence) {
        memberService.findExistingMember(memberId);
        aiUsageService.increaseUsage(memberId);
        return openAiPromptApi.parseNaturalLanguage(sentence);
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
