package com.dh.ondot.schedule.infra.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmapTransitRouteApiResponse(
    val metaData: MetaData,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MetaData(
        val plan: Plan,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Plan(
        val itineraries: List<Itinerary>,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Itinerary(
        val totalTime: Int,
        val transferCount: Int,
        val walkDistance: Double,
        val walkTime: Int,
        val totalDistance: Double,
        val pathType: Int,
    )
}
