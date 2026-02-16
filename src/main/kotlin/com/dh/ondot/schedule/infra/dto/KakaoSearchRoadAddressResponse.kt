package com.dh.ondot.schedule.infra.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoSearchRoadAddressResponse(
    @param:JsonProperty("documents") val documents: List<DocumentsResponse>,
) {
    data class DocumentsResponse(
        @param:JsonProperty("road_address") val roadAddress: RoadAddressResponse?,
    ) {
        data class RoadAddressResponse(
            @param:JsonProperty("address_name") val addressName: String,
            @param:JsonProperty("building_name") val buildingName: String?,
            @param:JsonProperty("x") val x: String?,
            @param:JsonProperty("y") val y: String?,
        )
    }
}
