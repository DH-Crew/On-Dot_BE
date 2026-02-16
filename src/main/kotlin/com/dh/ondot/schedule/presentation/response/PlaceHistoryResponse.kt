package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.PlaceHistory
import java.time.LocalDateTime

data class PlaceHistoryResponse(
    val title: String?,
    val roadAddress: String?,
    val longitude: Double?,
    val latitude: Double?,
    val searchedAt: LocalDateTime?,
) {
    companion object {
        @JvmStatic
        fun from(history: PlaceHistory): PlaceHistoryResponse {
            return PlaceHistoryResponse(
                title = history.title,
                roadAddress = history.roadAddress,
                longitude = history.longitude,
                latitude = history.latitude,
                searchedAt = TimeUtils.toSeoulDateTime(history.searchedAt),
            )
        }

        @JvmStatic
        fun fromList(list: List<PlaceHistory>): List<PlaceHistoryResponse> {
            return list.map { from(it) }
        }
    }
}
