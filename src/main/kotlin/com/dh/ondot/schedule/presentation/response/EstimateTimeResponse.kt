package com.dh.ondot.schedule.presentation.response

data class EstimateTimeResponse(
    val estimatedTime: Int,
) {
    companion object {
        fun from(estimatedTime: Int): EstimateTimeResponse {
            return EstimateTimeResponse(estimatedTime)
        }
    }
}
