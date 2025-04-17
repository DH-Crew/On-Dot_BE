package com.dh.ondot.schedule.api.request;

import com.dh.ondot.schedule.domain.Place;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceDto(
        String title,

        @NotBlank String roadAddress,

        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,

        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude
) {
    public static PlaceDto from(Place place) {
        return new PlaceDto(place.getTitle(), place.getRoadAddress(), place.getLongitude(), place.getLatitude());
    }
}
