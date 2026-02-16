package com.dh.ondot.member.presentation.request

import jakarta.validation.constraints.NotBlank

data class UpdateMapProviderRequest(
    @field:NotBlank
    val mapProvider: String,
)
