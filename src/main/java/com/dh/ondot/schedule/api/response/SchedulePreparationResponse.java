package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.Schedule;

public record SchedulePreparationResponse(
        boolean isMedicationRequired,
        String preparationNote
) {
    public static SchedulePreparationResponse from(Schedule s) {
        return new SchedulePreparationResponse(
                s.isMedicationRequired(),
                s.getPreparationNote()
        );
    }
}
