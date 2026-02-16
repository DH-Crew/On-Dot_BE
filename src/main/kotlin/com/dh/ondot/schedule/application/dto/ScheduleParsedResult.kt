package com.dh.ondot.schedule.application.dto

import java.time.LocalDateTime

data class ScheduleParsedResult(
    val departurePlaceTitle: String?,
    val appointmentAt: LocalDateTime?,
)
