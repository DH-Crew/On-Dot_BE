package com.dh.ondot.schedule.presentation.request

import jakarta.validation.constraints.NotBlank

data class ScheduleParsedRequest(
    @field:NotBlank
    val text: String,
)
