package com.dh.ondot.schedule.api;

import com.dh.ondot.schedule.api.request.*;
import com.dh.ondot.schedule.api.response.*;
import com.dh.ondot.schedule.api.swagger.ScheduleSwagger;
import com.dh.ondot.schedule.application.ScheduleCommandFacade;
import com.dh.ondot.schedule.application.ScheduleQueryFacade;
import com.dh.ondot.schedule.application.dto.UpdateScheduleResult;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController implements ScheduleSwagger {
    private final ScheduleQueryFacade scheduleQueryFacade;
    private final ScheduleCommandFacade scheduleCommandFacade;
    private final RouteService routeService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ScheduleCreateResponse createSchedule(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        Schedule schedule = scheduleCommandFacade.createSchedule(memberId, request);

        return ScheduleCreateResponse.of(schedule);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/quick")
    public void createQuickSchedule(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody QuickScheduleCreateRequest request
    ) {
        scheduleCommandFacade.createQuickSchedule(memberId, request);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/quickV1")
    public void createQuickScheduleV1(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody QuickScheduleCreateRequest request
    ) {
        scheduleCommandFacade.createQuickScheduleV1(memberId, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/voice")
    public ScheduleParsedResponse parseVoiceSchedule(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody ScheduleParsedRequest request
    ) {
        return scheduleCommandFacade.parseVoiceSchedule(memberId, request.text());
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/estimate-time")
    public EstimateTimeResponse estimateTravelTime(
            @Valid @RequestBody EstimateTimeRequest request
    ) {
        Integer estimatedTime = routeService.calculateRouteTime(
                request.startLongitude(), request.startLatitude(),
                request.endLongitude(), request.endLatitude()
        );

        return EstimateTimeResponse.from(estimatedTime);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}")
    public ScheduleDetailResponse getSchedule(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long scheduleId
    ) {
        Schedule schedule = scheduleQueryFacade.findOne(memberId, scheduleId);

        return ScheduleDetailResponse.from(schedule);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public HomeScheduleListResponse getSchedules(
            @RequestAttribute("memberId") Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return scheduleQueryFacade.findAll(memberId, pageable);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleUpdateResponse> updateSchedule(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request
    ) {
        UpdateScheduleResult result = scheduleCommandFacade.updateSchedule(memberId, scheduleId, request);
        HttpStatus status = result.needsDepartureTimeRecalculation() ? HttpStatus.ACCEPTED : HttpStatus.OK;

        return ResponseEntity.status(status).body(ScheduleUpdateResponse.of(result.schedule()));
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{scheduleId}/alarm")
    public AlarmSwitchResponse switchAlarm(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody AlarmSwitchRequest request
    ) {
        Schedule schedule = scheduleCommandFacade.switchAlarm(memberId, scheduleId, request.isEnabled());

        return AlarmSwitchResponse.from(schedule);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{scheduleId}")
    public void deleteSchedule(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long scheduleId
    ) {
        scheduleCommandFacade.deleteSchedule(memberId, scheduleId);
    }
}
