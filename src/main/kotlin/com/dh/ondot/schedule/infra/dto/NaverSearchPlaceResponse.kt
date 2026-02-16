package com.dh.ondot.schedule.infra.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NaverSearchPlaceResponse(
    @param:JsonProperty("items") val places: List<NaverPlace>,
) {
    data class NaverPlace(
        val title: String,
        val roadAddress: String?,
        val mapx: Int,
        val mapy: Int,
    )
}
