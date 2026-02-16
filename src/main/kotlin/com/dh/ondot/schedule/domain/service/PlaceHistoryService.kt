package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.PlaceHistory
import com.dh.ondot.schedule.infra.redis.PlaceHistoryRedisRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PlaceHistoryService(
    private val repository: PlaceHistoryRedisRepository,
) {
    fun record(
        memberId: Long, title: String?, roadAddress: String,
        lon: Double, lat: Double,
    ) {
        val finalTitle = if (title.isNullOrBlank()) roadAddress else title

        repository.push(PlaceHistory.of(memberId, finalTitle, roadAddress, lon, lat))
    }

    fun recent(memberId: Long): List<PlaceHistory> =
        repository.findRecent(memberId)

    fun delete(memberId: Long, searchedAt: LocalDateTime) {
        val instant = TimeUtils.toInstant(searchedAt)
        repository.removeByTimestamp(memberId, instant)
    }
}
