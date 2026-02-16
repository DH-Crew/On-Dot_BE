package com.dh.ondot.schedule.api.request

import jakarta.validation.constraints.NotNull

data class AlarmSwitchRequest(
    @field:NotNull
    val isEnabled: Boolean,
)
