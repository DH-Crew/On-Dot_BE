package com.dh.ondot.schedule.presentation.request

import jakarta.validation.constraints.NotBlank

data class EverytimeValidateRequest(
    @field:NotBlank(message = "everytimeUrl은 필수입니다.")
    val everytimeUrl: String,
)
