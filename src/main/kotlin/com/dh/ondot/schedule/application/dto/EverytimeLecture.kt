package com.dh.ondot.schedule.application.dto

import java.time.LocalTime

data class EverytimeLecture(
    val name: String,
    val day: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val place: String,
)
