package com.dh.ondot.schedule.application.dto;

public record PlaceSearchResult(
        String title,
        String roadAddress,
        Double longitude,
        Double latitude
) {
}
