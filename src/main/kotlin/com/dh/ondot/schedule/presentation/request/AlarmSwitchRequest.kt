package com.dh.ondot.schedule.presentation.request

import jakarta.validation.constraints.NotNull

data class AlarmSwitchRequest(
    @field:NotNull
    val isEnabled: Boolean,
)
