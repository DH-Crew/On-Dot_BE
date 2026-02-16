package com.dh.ondot.schedule.presentation.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class QuickScheduleCreateRequest(
    @field:NotNull
    val appointmentAt: LocalDateTime,

    @field:NotNull @field:Valid
    val departurePlace: PlaceDto,

    @field:NotNull @field:Valid
    val arrivalPlace: PlaceDto,
)
