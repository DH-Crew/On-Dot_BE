package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.application.dto.PlaceSearchResult;

import java.util.List;

public record PlaceSearchResponse(
        String title,
        String roadAddress,
        Double longitude,
        Double latitude
) {
    public static PlaceSearchResponse from(PlaceSearchResult result) {
        return new PlaceSearchResponse(
                result.getTitle(),
                result.getRoadAddress(),
                result.getLongitude(),
                result.getLatitude()
        );
    }

    public static List<PlaceSearchResponse> fromList(List<PlaceSearchResult> results) {
        return results.stream()
                .map(PlaceSearchResponse::from)
                .toList();
    }
}
