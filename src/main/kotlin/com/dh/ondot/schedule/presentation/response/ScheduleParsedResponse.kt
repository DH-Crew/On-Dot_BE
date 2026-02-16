package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.schedule.application.dto.ScheduleParsedResult
import java.time.LocalDateTime

data class ScheduleParsedResponse(
    val departurePlaceTitle: String?,
    val appointmentAt: LocalDateTime?,
) {
    companion object {
        fun from(result: ScheduleParsedResult): ScheduleParsedResponse =
            ScheduleParsedResponse(result.departurePlaceTitle, result.appointmentAt)
    }
}
