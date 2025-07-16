package com.dh.ondot.schedule.application.dto;

import com.dh.ondot.schedule.domain.Schedule;

public record UpdateScheduleResult(
        Schedule schedule,
        boolean needsDepartureTimeRecalculation
) {
}
