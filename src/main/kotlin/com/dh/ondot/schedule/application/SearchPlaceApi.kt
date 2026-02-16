package com.dh.ondot.schedule.application

import com.dh.ondot.schedule.application.dto.PlaceSearchResult

interface SearchPlaceApi {
    fun search(query: String): List<PlaceSearchResult>
}
