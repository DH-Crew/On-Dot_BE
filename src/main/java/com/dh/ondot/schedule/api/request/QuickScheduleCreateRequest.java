package com.dh.ondot.schedule.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record QuickScheduleCreateRequest(
        @NotNull LocalDateTime appointmentAt,

        @NotNull @Valid PlaceDto departurePlace,

        @NotNull @Valid PlaceDto arrivalPlace
) {
}
