package com.dh.ondot.schedule.application.command

import com.dh.ondot.schedule.domain.enums.TransportType
import java.time.DayOfWeek
import java.time.LocalTime

data class CreateEverytimeScheduleCommand(
    val selectedLectures: List<SelectedLecture>,
    val departurePlace: CreateScheduleCommand.PlaceInfo,
    val arrivalPlace: CreateScheduleCommand.PlaceInfo,
    val transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
) {
    data class SelectedLecture(
        val day: DayOfWeek,
        val startTime: LocalTime,
    )
}
