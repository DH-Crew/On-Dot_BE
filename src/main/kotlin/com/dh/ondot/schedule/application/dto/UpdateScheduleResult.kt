package com.dh.ondot.schedule.application.dto

import com.dh.ondot.schedule.domain.Schedule

data class UpdateScheduleResult(
    val schedule: Schedule,
    val needsDepartureTimeRecalculation: Boolean,
)
