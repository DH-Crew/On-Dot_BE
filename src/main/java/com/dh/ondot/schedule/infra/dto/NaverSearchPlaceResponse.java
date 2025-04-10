package com.dh.ondot.schedule.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NaverSearchPlaceResponse(
        @JsonProperty("items") List<NaverPlace> places
) {
    public record NaverPlace(
            String title,
            String roadAddress,
            Integer mapx,
            Integer mapy
    ) {}
}
