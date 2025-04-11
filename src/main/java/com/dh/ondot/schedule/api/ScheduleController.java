package com.dh.ondot.schedule.api;

import com.dh.ondot.schedule.api.request.ScheduleCreateRequest;
import com.dh.ondot.schedule.api.response.ScheduleCreateResponse;
import com.dh.ondot.schedule.app.ScheduleFacade;
import com.dh.ondot.schedule.domain.Schedule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {
    private final ScheduleFacade scheduleFacade;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ScheduleCreateResponse createSchedule(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        Schedule schedule = scheduleFacade.createSchedule(memberId, request);

        return ScheduleCreateResponse.of(schedule);
    }
}
