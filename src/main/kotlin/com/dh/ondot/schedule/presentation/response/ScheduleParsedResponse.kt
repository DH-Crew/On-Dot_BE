package com.dh.ondot.schedule.presentation.response

import java.time.LocalDateTime

data class ScheduleParsedResponse(
    val departurePlaceTitle: String?,
    val appointmentAt: LocalDateTime?,
)
