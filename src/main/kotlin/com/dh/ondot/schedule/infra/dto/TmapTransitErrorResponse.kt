package com.dh.ondot.schedule.infra.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmapTransitErrorResponse(
    val result: Result?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Result(
        val status: Int,
        val message: String,
    )
}
