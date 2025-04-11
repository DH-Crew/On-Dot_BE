package com.dh.ondot.schedule.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceDto(
        String title,

        @NotBlank String roadAddress,

        @NotNull Double longitude,

        @NotNull Double latitude
) {
}
