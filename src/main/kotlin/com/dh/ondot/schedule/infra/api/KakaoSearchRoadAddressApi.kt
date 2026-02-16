package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.application.SearchRoadAddressApi
import com.dh.ondot.schedule.application.dto.PlaceSearchResult
import com.dh.ondot.schedule.infra.dto.KakaoSearchRoadAddressResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class KakaoSearchRoadAddressApi : SearchRoadAddressApi {
    @Value("\${oauth2.client.registration.kakao.client_id}")
    lateinit var authorizationKey: String

    override fun search(query: String): List<PlaceSearchResult> {
        val restClient = RestClient.builder()
            .baseUrl("https://dapi.kakao.com/v2/local/search/address.json")
            .defaultHeader("Authorization", "KakaoAK $authorizationKey")
            .build()

        val response = restClient.get()
            .uri { uriBuilder -> uriBuilder.queryParam("query", query).build() }
            .retrieve()
            .body(KakaoSearchRoadAddressResponse::class.java)

        return response!!.documents.stream()
            .filter { doc ->
                doc.roadAddress != null
                    && doc.roadAddress.x != null
                    && doc.roadAddress.y != null
            }
            .map { doc ->
                PlaceSearchResult(
                    doc.roadAddress!!.addressName,
                    doc.roadAddress.addressName,
                    doc.roadAddress.x!!.toDouble(),
                    doc.roadAddress.y!!.toDouble()
                )
            }
            .toList()
    }
}
