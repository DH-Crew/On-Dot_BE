package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.application.SearchPlaceApi
import com.dh.ondot.schedule.application.dto.PlaceSearchResult
import com.dh.ondot.schedule.infra.dto.NaverSearchPlaceResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class NaverSearchPlaceApi : SearchPlaceApi {
    @Value("\${naver.client.client-id}")
    lateinit var clientId: String

    @Value("\${naver.client.client-secret}")
    lateinit var clientSecret: String

    override fun search(query: String): List<PlaceSearchResult> {
        val refinedQuery = refineQuery(query)

        val restClient = RestClient.builder()
            .baseUrl("https://openapi.naver.com/v1/search/local.json")
            .defaultHeader("X-Naver-Client-Id", clientId)
            .defaultHeader("X-Naver-Client-Secret", clientSecret)
            .build()

        val response = restClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .queryParam("query", refinedQuery)
                    .queryParam("display", 5)
                    .build()
            }
            .retrieve()
            .body(NaverSearchPlaceResponse::class.java)

        return response!!.places.stream()
            .map { place ->
                PlaceSearchResult(
                    place.title.replace(Regex("<.*?>"), ""),
                    if (place.roadAddress == null || place.roadAddress.isBlank())
                        place.title.replace(Regex("<.*?>"), "")
                    else
                        place.roadAddress,
                    place.mapx / NAVER_COORDINATE_SCALE,
                    place.mapy / NAVER_COORDINATE_SCALE
                )
            }
            .toList()
    }

    private fun refineQuery(query: String): String {
        if (query.contains("가톨") || query.contains("가톨릭")) {
            return "가톨릭대"
        }
        return query
    }

    companion object {
        val NAVER_COORDINATE_SCALE = 10_000_000.0
    }
}
