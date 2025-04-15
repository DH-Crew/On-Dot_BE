package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.PlaceHistory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record PlaceHistoryResponse(
        String title,
        Double longitude,
        Double latitude,
        String searchedAt
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static PlaceHistoryResponse from(PlaceHistory history) {
        return new PlaceHistoryResponse(
                history.title(),
                history.longitude(),
                history.latitude(),
                FORMATTER.format(history.searchedAt().atZone(KST).toLocalDateTime())
        );
    }
    public static List<PlaceHistoryResponse> fromList(List<PlaceHistory> list) {
        return list.stream().map(PlaceHistoryResponse::from).toList();
    }
}
