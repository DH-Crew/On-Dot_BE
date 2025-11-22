package com.dh.ondot.schedule.domain.event;

import java.time.LocalDateTime;

public record QuickScheduleRequestedEvent(
        Long memberId,
        Long departurePlaceId,
        Long arrivalPlaceId,
        LocalDateTime appointmentAt
) {}
