package com.dh.ondot.schedule.domain;

import java.time.LocalDateTime;

public record PlaceHistory(
        Long memberId,
        String title,
        Double longitude,
        Double latitude,
        LocalDateTime searchedAt
) {
    public static PlaceHistory of(
            Long memberId, String title,
            Double longitude, Double latitude
    ) {
        return new PlaceHistory(
                memberId,
                title,
                longitude,
                latitude,
                LocalDateTime.now()
        );
    }
}
