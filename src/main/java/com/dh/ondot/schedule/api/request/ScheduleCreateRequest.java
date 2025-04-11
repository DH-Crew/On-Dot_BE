package com.dh.ondot.schedule.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ScheduleCreateRequest(
        @NotBlank String title,

        @NotNull Boolean isRepeat,

        @NotNull @Size(min = 1, max = 7)
        List<@Min(1) @Max(7) Integer> repeatDay,

        @NotNull LocalDateTime appointmentAt,

        @NotNull @Valid PlaceDto departurePlace,

        @NotNull @Valid PlaceDto arrivalPlace,

        @NotNull @Valid PreparationAlarmDto preparationAlarm,

        @NotNull @Valid DepartureAlarmDto departureAlarm
) {
    public record PreparationAlarmDto(
            @NotBlank String alarmMode,

            @NotNull Boolean isEnabled,

            @NotNull LocalTime triggeredAt,

            @NotBlank String mission,

            @NotNull Boolean isSnoozeEnabled,

            @NotNull @Min(1) @Max(60) Integer snoozeInterval,

            @NotNull @Min(-1) @Max(10) Integer snoozeCount,

            @NotBlank String soundCategory,

            @NotBlank String ringTone,

            @NotNull @Min(1) @Max(10) Integer volume
    ) {}

    public record DepartureAlarmDto(
            @NotBlank String alarmMode,

            @NotNull LocalTime triggeredAt,

            @NotNull Boolean isSnoozeEnabled,

            @NotNull @Min(1) @Max(60) Integer snoozeInterval,

            @NotNull @Min(-1) @Max(10) Integer snoozeCount,

            @NotBlank String soundCategory,

            @NotBlank String ringTone,

            @NotNull @Min(1) @Max(10) Integer volume
    ) {}
}
