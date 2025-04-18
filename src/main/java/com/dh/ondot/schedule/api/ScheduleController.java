package com.dh.ondot.schedule.api;

import com.dh.ondot.schedule.api.request.*;
import com.dh.ondot.schedule.api.response.*;
import com.dh.ondot.schedule.api.swagger.ScheduleSwagger;
import com.dh.ondot.schedule.app.ParseFacade;
import com.dh.ondot.schedule.app.ScheduleCommandFacade;
import com.dh.ondot.schedule.app.ScheduleQueryFacade;
import com.dh.ondot.schedule.app.dto.UpdateScheduleResult;
import com.dh.ondot.schedule.domain.Schedule;
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
    private final ParseFacade parseFacade;

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
    @PostMapping("/voice")
    public void createVoiceSchedule(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody VoiceScheduleCreateRequest request
    ) {
        scheduleCommandFacade.createVoiceSchedule(memberId, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/nlp")
    public ScheduleParsedResponse parse(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody ScheduleParsedRequest request
    ) {
        return parseFacade.parse(memberId, request.text());
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
