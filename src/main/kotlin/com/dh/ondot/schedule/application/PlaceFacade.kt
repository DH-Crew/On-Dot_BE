package com.dh.ondot.schedule.application

import com.dh.ondot.schedule.application.dto.PlaceSearchResult
import com.dh.ondot.schedule.domain.PlaceHistory
import com.dh.ondot.schedule.domain.service.PlaceHistoryService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PlaceFacade(
    private val placeHistoryService: PlaceHistoryService,
    private val searchPlaceApi: SearchPlaceApi,
    private val searchRoadAddressApi: SearchRoadAddressApi,
) {
    fun saveHistory(
        memberId: Long, title: String,
        roadAddr: String, longitude: Double, latitude: Double,
    ) {
        placeHistoryService.record(
            memberId, title, roadAddr,
            longitude, latitude,
        )
    }

    fun getHistory(memberId: Long): List<PlaceHistory> {
        return placeHistoryService.recent(memberId)
    }

    fun deleteHistory(memberId: Long, searchedAt: LocalDateTime) {
        placeHistoryService.delete(memberId, searchedAt)
    }

    fun searchPlaces(query: String): List<PlaceSearchResult> {
        val placeResults = searchPlaceApi.search(query)
        val roadAddressResults = searchRoadAddressApi.search(query)

        return mergeResultsUsingRoundRobin(placeResults, roadAddressResults)
    }

    private fun mergeResultsUsingRoundRobin(
        first: List<PlaceSearchResult>,
        second: List<PlaceSearchResult>,
    ): List<PlaceSearchResult> {
        val merged = mutableListOf<PlaceSearchResult>()
        val seen = mutableSetOf<String>()

        val firstIterator = first.iterator()
        val secondIterator = second.iterator()

        while (firstIterator.hasNext() || secondIterator.hasNext()) {
            if (firstIterator.hasNext()) {
                val result = firstIterator.next()
                if (seen.add(createKey(result))) {
                    merged.add(result)
                }
            }
            if (secondIterator.hasNext()) {
                val result = secondIterator.next()
                if (seen.add(createKey(result))) {
                    merged.add(result)
                }
            }
        }

        return merged
    }

    private fun createKey(result: PlaceSearchResult): String {
        val title = result.title ?: ""
        val roadAddress = result.roadAddress ?: ""
        return "$title|$roadAddress"
    }
}
