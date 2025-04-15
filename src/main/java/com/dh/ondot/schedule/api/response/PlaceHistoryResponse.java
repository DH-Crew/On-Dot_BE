package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.PlaceHistory;

import java.util.List;

public record PlaceHistoryResponse(
        String title,
        Double longitude,
        Double latitude,
        String searchedAt
) {
    public static PlaceHistoryResponse from(PlaceHistory history) {
        return new PlaceHistoryResponse(
                history.title(),
                history.longitude(),
                history.latitude(),
                history.searchedAt().toString());
    }
    public static List<PlaceHistoryResponse> fromList(List<PlaceHistory> list) {
        return list.stream().map(PlaceHistoryResponse::from).toList();
    }
}
