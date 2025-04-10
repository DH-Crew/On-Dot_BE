package com.dh.ondot.schedule.app.dto;

public record PlaceSearchResult(
        String title,
        String roadAddress,
        Double longitude,
        Double latitude
) {
}
