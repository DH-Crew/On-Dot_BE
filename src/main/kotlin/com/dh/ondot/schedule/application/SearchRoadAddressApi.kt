package com.dh.ondot.schedule.application

import com.dh.ondot.schedule.application.dto.PlaceSearchResult

interface SearchRoadAddressApi {
    fun search(query: String): List<PlaceSearchResult>
}
