package com.dh.ondot.schedule.presentation.request

import com.dh.ondot.schedule.application.command.CreateQuickScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
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
) {
    fun toCommand(): CreateQuickScheduleCommand = CreateQuickScheduleCommand(
        appointmentAt = appointmentAt,
        departurePlace = CreateScheduleCommand.PlaceInfo(
            departurePlace.title, departurePlace.roadAddress,
            departurePlace.longitude, departurePlace.latitude,
        ),
        arrivalPlace = CreateScheduleCommand.PlaceInfo(
            arrivalPlace.title, arrivalPlace.roadAddress,
            arrivalPlace.longitude, arrivalPlace.latitude,
        ),
    )
}
