package com.dh.ondot.schedule.application.command

import com.dh.ondot.schedule.domain.enums.TransportType
import java.time.LocalDateTime

data class GenerateAlarmCommand(
    val appointmentAt: LocalDateTime,
    val startLongitude: Double,
    val startLatitude: Double,
    val endLongitude: Double,
    val endLatitude: Double,
    val transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
)
