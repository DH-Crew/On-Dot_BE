package com.dh.ondot.member.presentation.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateHomeAddressRequest(
    @field:NotBlank
    val roadAddress: String,

    @field:NotNull @field:DecimalMin("-180.0") @field:DecimalMax("180.0")
    val longitude: Double,

    @field:NotNull @field:DecimalMin("-90.0") @field:DecimalMax("90.0")
    val latitude: Double,
)
