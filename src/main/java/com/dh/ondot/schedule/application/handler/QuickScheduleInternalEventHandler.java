package com.dh.ondot.schedule.application.handler;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent;
import com.dh.ondot.schedule.domain.repository.PlaceRepository;
import com.dh.ondot.schedule.domain.service.RouteService;
import com.dh.ondot.schedule.domain.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class QuickScheduleInternalEventHandler {
    private final MemberService memberService;
    private final RouteService routeService;
    private final ScheduleService scheduleService;
    private final PlaceRepository placeRepository;

    @Transactional
    public void handleEvent(QuickScheduleRequestedEvent event) {
        Member member = memberService.getMemberIfExists(event.getMemberId());
        Place dep = placeRepository.getReferenceById(event.getDeparturePlaceId());
        Place arr = placeRepository.getReferenceById(event.getArrivalPlaceId());

        int estimatedTimeMin = routeService.calculateRouteTime(
                dep.getLongitude(), dep.getLatitude(),
                arr.getLongitude(), arr.getLatitude()
        );

        Schedule schedule = scheduleService.setupSchedule(
                member, event.getAppointmentAt(), estimatedTimeMin
        );
        schedule.registerPlaces(dep, arr);

        scheduleService.saveSchedule(schedule);
    }
}
