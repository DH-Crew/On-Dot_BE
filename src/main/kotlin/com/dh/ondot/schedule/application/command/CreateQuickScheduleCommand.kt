package com.dh.ondot.schedule.application.command

import java.time.LocalDateTime

data class CreateQuickScheduleCommand(
    val appointmentAt: LocalDateTime,
    val departurePlace: CreateScheduleCommand.PlaceInfo,
    val arrivalPlace: CreateScheduleCommand.PlaceInfo,
)
