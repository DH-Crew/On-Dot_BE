package com.dh.ondot.schedule.presentation.request

import com.dh.ondot.schedule.application.command.CreateScheduleCommand
import com.dh.ondot.schedule.domain.enums.TransportType
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ScheduleCreateRequest(
    @field:NotBlank
    val title: String,

    @field:NotNull
    val isRepeat: Boolean,

    // 1: 일요일, 2: 월요일, ..., 7: 토요일
    @field:NotNull @field:Size(max = 7)
    val repeatDays: List<@Min(1) @Max(7) Int>,

    @field:NotNull
    val appointmentAt: LocalDateTime,

    @field:NotNull
    val isMedicationRequired: Boolean,

    @field:Size(max = 100)
    val preparationNote: String?,

    val transportType: TransportType? = null,

    @field:NotNull @field:Valid
    val departurePlace: PlaceDto,

    @field:NotNull @field:Valid
    val arrivalPlace: PlaceDto,

    @field:NotNull @field:Valid
    val preparationAlarm: PreparationAlarmDto,

    @field:NotNull @field:Valid
    val departureAlarm: DepartureAlarmDto,
) {
    data class PreparationAlarmDto(
        @field:NotBlank
        val alarmMode: String,

        @field:NotNull
        val isEnabled: Boolean,

        @field:NotNull
        val triggeredAt: LocalDateTime,

//        @field:NotBlank
//        val mission: String,

        @field:NotNull
        val isSnoozeEnabled: Boolean,

        @field:NotNull @field:Min(1) @field:Max(60)
        val snoozeInterval: Int,

        @field:NotNull @field:Min(-1) @field:Max(10)
        val snoozeCount: Int,

        @field:NotBlank
        val soundCategory: String,

        @field:NotBlank
        val ringTone: String,

        @field:NotNull @field:Min(0) @field:Max(1)
        val volume: Double,
    )

    data class DepartureAlarmDto(
        @field:NotBlank
        val alarmMode: String,

        @field:NotNull
        val triggeredAt: LocalDateTime,

        @field:NotNull
        val isSnoozeEnabled: Boolean,

        @field:NotNull @field:Min(1) @field:Max(60)
        val snoozeInterval: Int,

        @field:NotNull @field:Min(-1) @field:Max(10)
        val snoozeCount: Int,

        @field:NotBlank
        val soundCategory: String,

        @field:NotBlank
        val ringTone: String,

        @field:NotNull @field:Min(0) @field:Max(1)
        val volume: Double,
    )

    fun toCommand(): CreateScheduleCommand = CreateScheduleCommand(
        title = title,
        isRepeat = isRepeat,
        repeatDays = repeatDays,
        appointmentAt = appointmentAt,
        isMedicationRequired = isMedicationRequired,
        preparationNote = preparationNote,
        transportType = transportType ?: TransportType.PUBLIC_TRANSPORT,
        departurePlace = CreateScheduleCommand.PlaceInfo(
            departurePlace.title, departurePlace.roadAddress,
            departurePlace.longitude, departurePlace.latitude,
        ),
        arrivalPlace = CreateScheduleCommand.PlaceInfo(
            arrivalPlace.title, arrivalPlace.roadAddress,
            arrivalPlace.longitude, arrivalPlace.latitude,
        ),
        preparationAlarm = CreateScheduleCommand.PreparationAlarmInfo(
            preparationAlarm.alarmMode, preparationAlarm.isEnabled,
            preparationAlarm.triggeredAt, preparationAlarm.isSnoozeEnabled,
            preparationAlarm.snoozeInterval, preparationAlarm.snoozeCount,
            preparationAlarm.soundCategory, preparationAlarm.ringTone,
            preparationAlarm.volume,
        ),
        departureAlarm = CreateScheduleCommand.DepartureAlarmInfo(
            departureAlarm.alarmMode, departureAlarm.triggeredAt,
            departureAlarm.isSnoozeEnabled, departureAlarm.snoozeInterval,
            departureAlarm.snoozeCount, departureAlarm.soundCategory,
            departureAlarm.ringTone, departureAlarm.volume,
        ),
    )
}
