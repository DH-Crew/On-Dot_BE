package com.dh.ondot.schedule.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.domain.PlaceHistory;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceHistoryResponse(
        String title,
        Double longitude,
        Double latitude,
        LocalDateTime searchedAt
) {
    public static PlaceHistoryResponse from(PlaceHistory history) {
        return new PlaceHistoryResponse(
                history.title(),
                history.longitude(),
                history.latitude(),
                TimeUtils.toSeoulDateTime(history.searchedAt())
        );
    }
    public static List<PlaceHistoryResponse> fromList(List<PlaceHistory> list) {
        return list.stream().map(PlaceHistoryResponse::from).toList();
    }
}
