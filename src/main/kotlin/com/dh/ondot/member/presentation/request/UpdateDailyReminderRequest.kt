package com.dh.ondot.member.presentation.request

import jakarta.validation.constraints.NotNull

data class UpdateDailyReminderRequest(
    @field:NotNull
    val enabled: Boolean,
)
