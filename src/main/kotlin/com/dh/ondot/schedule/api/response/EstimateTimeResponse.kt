package com.dh.ondot.schedule.api.response

data class EstimateTimeResponse(
    val estimatedTime: Int?,
) {
    companion object {
        @JvmStatic
        fun from(estimatedTime: Int?): EstimateTimeResponse {
            return EstimateTimeResponse(estimatedTime)
        }
    }
}
