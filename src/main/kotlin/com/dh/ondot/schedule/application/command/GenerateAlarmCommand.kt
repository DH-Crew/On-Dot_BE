package com.dh.ondot.schedule.application.command

import java.time.LocalDateTime

data class GenerateAlarmCommand(
    val appointmentAt: LocalDateTime,
    val startLongitude: Double,
    val startLatitude: Double,
    val endLongitude: Double,
    val endLatitude: Double,
)
