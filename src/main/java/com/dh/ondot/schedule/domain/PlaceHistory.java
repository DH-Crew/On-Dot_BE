package com.dh.ondot.schedule.domain;

import java.time.Instant;

public record PlaceHistory(
        Long memberId,
        String title,
        Double longitude,
        Double latitude,
        Instant searchedAt
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
                Instant.now()
        );
    }
}
