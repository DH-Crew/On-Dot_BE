package com.dh.ondot.schedule.api.response

import com.dh.ondot.schedule.domain.Schedule

data class SchedulePreparationResponse(
    val isMedicationRequired: Boolean,
    val preparationNote: String?,
) {
    companion object {
        @JvmStatic
        fun from(s: Schedule): SchedulePreparationResponse {
            return SchedulePreparationResponse(
                isMedicationRequired = s.isMedicationRequired,
                preparationNote = s.preparationNote,
            )
        }
    }
}
