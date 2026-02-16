package com.dh.ondot.schedule.api.request

import jakarta.validation.constraints.NotBlank

data class ScheduleParsedRequest(
    @field:NotBlank
    val text: String,
)
