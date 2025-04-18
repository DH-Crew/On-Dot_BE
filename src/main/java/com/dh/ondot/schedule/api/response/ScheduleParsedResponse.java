package com.dh.ondot.schedule.api.response;

import java.time.LocalDateTime;

public record ScheduleParsedResponse(
        String departurePlaceTitle,
        LocalDateTime appointmentAt
) {
}
