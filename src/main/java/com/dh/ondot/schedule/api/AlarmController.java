package com.dh.ondot.schedule.api;

import com.dh.ondot.schedule.api.response.LatestAlarmResponse;
import com.dh.ondot.schedule.api.swagger.AlarmSwagger;
import com.dh.ondot.schedule.app.AlarmFacade;
import com.dh.ondot.schedule.domain.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarms")
public class AlarmController implements AlarmSwagger {
    private final AlarmFacade alarmFacade;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/latest")
    public LatestAlarmResponse getLatestAlarms(
            @RequestAttribute("memberId") Long memberId
    ) {
        Schedule schedule = alarmFacade.getLatestAlarms(memberId);

        return LatestAlarmResponse.from(
                schedule.getPreparationAlarm(),
                schedule.getDepartureAlarm()
        );
    }
}
