package com.dh.ondot.schedule.api.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class RecordAlarmTriggerRequest(
    @field:NotNull(message = "scheduleId는 필수입니다.")
    val scheduleId: Long,

    @field:NotNull(message = "alarmId는 필수입니다.")
    val alarmId: Long,

    @field:NotNull(message = "action은 필수입니다.")
    @field:Pattern(regexp = "SCHEDULED|STOP|SNOOZE|VIEW_ROUTE|START_PREPARE", message = "action은 SCHEDULED, STOP, SNOOZE, VIEW_ROUTE, START_PREPARE 중 하나여야 합니다.")
    val action: String,
)
