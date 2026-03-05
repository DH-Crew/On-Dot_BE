package com.dh.ondot.schedule.presentation.request

import com.dh.ondot.schedule.application.command.CreateEverytimeScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
import com.dh.ondot.schedule.domain.enums.TransportType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class EverytimeScheduleCreateRequest(
    @field:NotEmpty(message = "selectedLectures는 필수입니다.")
    @field:Valid
    val selectedLectures: List<SelectedLectureDto>,

    @field:NotNull @field:Valid
    val departurePlace: PlaceDto,

    @field:NotNull @field:Valid
    val arrivalPlace: PlaceDto,

    val transportType: TransportType? = null,
) {
    fun toCommand(): CreateEverytimeScheduleCommand = CreateEverytimeScheduleCommand(
        selectedLectures = selectedLectures.map {
            CreateEverytimeScheduleCommand.SelectedLecture(
                day = it.day,
                startTime = it.startTime,
            )
        },
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
