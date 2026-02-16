package com.dh.ondot.schedule.application.command

import java.time.LocalDateTime

data class QuickScheduleCommand(
    val memberId: Long,
    val appointmentAt: LocalDateTime,
    val departure: PlaceInfo,
    val arrival: PlaceInfo,
) {
    data class PlaceInfo(
        val title: String,
        val roadAddress: String,
        val longitude: Double,
        val latitude: Double,
    )
}
