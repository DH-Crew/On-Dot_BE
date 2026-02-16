package com.dh.ondot.schedule.api.response

import com.dh.ondot.schedule.application.dto.PlaceSearchResult

data class PlaceSearchResponse(
    val title: String?,
    val roadAddress: String?,
    val longitude: Double?,
    val latitude: Double?,
) {
    companion object {
        @JvmStatic
        fun from(result: PlaceSearchResult): PlaceSearchResponse {
            return PlaceSearchResponse(
                title = result.title,
                roadAddress = result.roadAddress,
                longitude = result.longitude,
                latitude = result.latitude,
            )
        }

        @JvmStatic
        fun fromList(results: List<PlaceSearchResult>): List<PlaceSearchResponse> {
            return results.map { from(it) }
        }
    }
}
