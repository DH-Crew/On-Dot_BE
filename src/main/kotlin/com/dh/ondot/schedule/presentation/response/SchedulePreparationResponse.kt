package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.schedule.domain.Schedule

data class SchedulePreparationResponse(
    val isMedicationRequired: Boolean,
    val preparationNote: String?,
) {
    companion object {
        fun from(s: Schedule): SchedulePreparationResponse {
            return SchedulePreparationResponse(
                isMedicationRequired = s.isMedicationRequired,
                preparationNote = s.preparationNote,
            )
        }
    }
}
