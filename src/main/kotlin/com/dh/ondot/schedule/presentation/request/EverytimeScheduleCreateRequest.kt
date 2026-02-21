package com.dh.ondot.schedule.presentation.request

import com.dh.ondot.schedule.application.command.CreateEverytimeScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
import com.dh.ondot.schedule.domain.enums.TransportType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EverytimeScheduleCreateRequest(
    @field:NotBlank(message = "everytimeUrl은 필수입니다.")
    val everytimeUrl: String,

    @field:NotNull @field:Valid
    val departurePlace: PlaceDto,

    @field:NotNull @field:Valid
    val arrivalPlace: PlaceDto,

    val transportType: TransportType? = null,
) {
    fun toCommand(): CreateEverytimeScheduleCommand = CreateEverytimeScheduleCommand(
        everytimeUrl = everytimeUrl,
        departurePlace = CreateScheduleCommand.PlaceInfo(
            departurePlace.title, departurePlace.roadAddress,
            departurePlace.longitude, departurePlace.latitude,
        ),
        arrivalPlace = CreateScheduleCommand.PlaceInfo(
            arrivalPlace.title, arrivalPlace.roadAddress,
            arrivalPlace.longitude, arrivalPlace.latitude,
        ),
        transportType = transportType ?: TransportType.PUBLIC_TRANSPORT,
    )
}
