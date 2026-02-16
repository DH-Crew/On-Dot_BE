package com.dh.ondot.schedule.application.command

import java.time.LocalDateTime

data class UpdateScheduleCommand(
    val title: String,
    val isRepeat: Boolean,
    val repeatDays: List<Int>,
    val appointmentAt: LocalDateTime,
    val departurePlace: CreateScheduleCommand.PlaceInfo,
    val arrivalPlace: CreateScheduleCommand.PlaceInfo,
    val preparationAlarm: CreateScheduleCommand.PreparationAlarmInfo,
    val departureAlarm: CreateScheduleCommand.DepartureAlarmInfo,
)
