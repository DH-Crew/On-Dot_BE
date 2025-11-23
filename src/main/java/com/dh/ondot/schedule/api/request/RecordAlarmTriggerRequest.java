package com.dh.ondot.schedule.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RecordAlarmTriggerRequest(
        @NotNull(message = "scheduleId는 필수입니다.")
        Long scheduleId,

        @NotNull(message = "alarmId는 필수입니다.")
        Long alarmId,

        @NotNull(message = "action은 필수입니다.")
        @Pattern(regexp = "STOP|SNOOZE|VIEW_ROUTE|START_PREPARE", message = "action은 STOP, SNOOZE, VIEW_ROUTE, START_PREPARE 중 하나여야 합니다.")
        String action
) {
}
