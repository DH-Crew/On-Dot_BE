package com.dh.ondot.schedule.api.request;

import jakarta.validation.constraints.NotBlank;

public record PlaceHistoryDeleteRequest(
        @NotBlank(message = "검색 시각은 필수입니다.")
        String searchedAt
) {
}
