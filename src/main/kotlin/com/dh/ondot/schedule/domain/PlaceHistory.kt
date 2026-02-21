package com.dh.ondot.schedule.domain

import java.time.Instant

data class PlaceHistory(
    val memberId: Long,
    val title: String,
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
    val searchedAt: Instant,
) {
    companion object {
        fun of(
            memberId: Long, title: String, roadAddress: String,
            longitude: Double, latitude: Double,
        ): PlaceHistory = PlaceHistory(
            memberId = memberId,
            title = title,
            roadAddress = roadAddress,
            longitude = longitude,
            latitude = latitude,
            searchedAt = Instant.now(),
        )
    }
}
