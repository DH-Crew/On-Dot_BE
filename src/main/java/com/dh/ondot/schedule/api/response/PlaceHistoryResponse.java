package com.dh.ondot.schedule.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.domain.PlaceHistory;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceHistoryResponse(
        String title,
        String roadAddress,
        Double longitude,
        Double latitude,
        LocalDateTime searchedAt
) {
    public static PlaceHistoryResponse from(PlaceHistory history) {
        return new PlaceHistoryResponse(
                history.getTitle(),
                history.getRoadAddress(),
                history.getLongitude(),
                history.getLatitude(),
                TimeUtils.toSeoulDateTime(history.getSearchedAt())
        );
    }
    public static List<PlaceHistoryResponse> fromList(List<PlaceHistory> list) {
        return list.stream().map(PlaceHistoryResponse::from).toList();
    }
}
