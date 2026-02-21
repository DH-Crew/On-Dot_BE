package com.dh.ondot.schedule.application.command

import com.dh.ondot.schedule.domain.enums.TransportType

data class CreateEverytimeScheduleCommand(
    val everytimeUrl: String,
    val departurePlace: CreateScheduleCommand.PlaceInfo,
    val arrivalPlace: CreateScheduleCommand.PlaceInfo,
    val transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
)
