package com.dh.ondot.schedule.infra.dto

import com.dh.ondot.schedule.infra.exception.TmapUnhandledException
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmapRouteApiResponse(
    val type: String?,
    val features: List<Feature>?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Feature(
        val type: String?,
        val properties: Properties?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Properties(
        val totalDistance: Int?,
        val totalTime: Int?,
        val totalFare: Int?,
    )

    fun getTotalTimeSeconds(): Int {
        val props = features?.firstOrNull()?.properties
            ?: throw TmapUnhandledException("TMAP 응답에 경로 정보가 없습니다.")
        return props.totalTime
            ?: throw TmapUnhandledException("TMAP 응답에 totalTime이 없습니다.")
    }
}
