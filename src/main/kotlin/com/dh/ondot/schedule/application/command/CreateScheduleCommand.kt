package com.dh.ondot.schedule.application.command

import com.dh.ondot.schedule.domain.enums.TransportType
import java.time.LocalDateTime

data class CreateScheduleCommand(
    val title: String,
    val isRepeat: Boolean,
    val repeatDays: List<Int>,
    val appointmentAt: LocalDateTime,
    val isMedicationRequired: Boolean,
    val preparationNote: String?,
    val transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
    val departurePlace: PlaceInfo,
    val arrivalPlace: PlaceInfo,
    val preparationAlarm: PreparationAlarmInfo,
    val departureAlarm: DepartureAlarmInfo,
) {
    data class PlaceInfo(
        val title: String,
        val roadAddress: String,
        val longitude: Double,
        val latitude: Double,
    )

    data class PreparationAlarmInfo(
        val alarmMode: String,
        val isEnabled: Boolean,
        val triggeredAt: LocalDateTime,
        val isSnoozeEnabled: Boolean,
        val snoozeInterval: Int,
        val snoozeCount: Int,
        val soundCategory: String,
        val ringTone: String,
        val volume: Double,
    )

    data class DepartureAlarmInfo(
        val alarmMode: String,
        val triggeredAt: LocalDateTime,
        val isSnoozeEnabled: Boolean,
        val snoozeInterval: Int,
        val snoozeCount: Int,
        val soundCategory: String,
        val ringTone: String,
        val volume: Double,
    )
}
