package com.dh.ondot.schedule.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KakaoSearchRoadAddressResponse(
        @JsonProperty("documents") List<DocumentsResponse> documents
) {
    public record DocumentsResponse(
            @JsonProperty("road_address") RoadAddressResponse roadAddress
    ) {
        public record RoadAddressResponse(
                @JsonProperty("address_name") String addressName,
                @JsonProperty("building_name") String buildingName,
                @JsonProperty("x") String x,
                @JsonProperty("y") String y
        ) {
        }
    }
}
