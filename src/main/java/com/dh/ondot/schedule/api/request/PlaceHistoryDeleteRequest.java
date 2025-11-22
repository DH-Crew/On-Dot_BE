package com.dh.ondot.schedule.api.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PlaceHistoryDeleteRequest(
        @NotNull(message = "검색 시각은 필수입니다.")
        LocalDateTime searchedAt
) {
}
