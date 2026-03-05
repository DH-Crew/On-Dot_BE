package com.dh.ondot.schedule.presentation.request

import jakarta.validation.constraints.NotNull
import java.time.DayOfWeek
import java.time.LocalTime

data class SelectedLectureDto(
    @field:NotNull
    val day: DayOfWeek,

    @field:NotNull
    val startTime: LocalTime,
)
