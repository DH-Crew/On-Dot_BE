package com.dh.ondot.schedule.api;

import com.dh.ondot.schedule.api.request.SetAlarmRequest;
import com.dh.ondot.schedule.api.response.SettingAlarmResponse;
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
    @PostMapping("/setting")
    public SettingAlarmResponse setAlarm(
            @RequestAttribute("memberId") Long memberId,
            @RequestBody SetAlarmRequest request
    ) {
        Schedule schedule = alarmFacade.generateAlarmSettingByRoute(
                memberId, request.appointmentAt(),
                request.startLongitude(), request.startLatitude(),
                request.endLongitude(), request.endLatitude()
        );

        return SettingAlarmResponse.from(
                schedule.getPreparationAlarm(),
                schedule.getDepartureAlarm()
        );
    }
}
