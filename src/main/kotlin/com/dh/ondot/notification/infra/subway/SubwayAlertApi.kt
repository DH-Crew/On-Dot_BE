package com.dh.ondot.notification.infra.subway

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class SubwayAlertApi {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val YYYYMMDD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    @Value("\${external-api.seoul-transportation.base-url}")
    private lateinit var baseUrl: String

    @Value("\${external-api.seoul-transportation.service-key}")
    private lateinit var serviceKey: String

    private lateinit var restClient: RestClient

    @PostConstruct
    fun init() {
        restClient = RestClient.create(baseUrl)
    }

    fun getRawAlertsByDate(date: LocalDate): String {
        val formatted = date.format(YYYYMMDD)
        val uri = UriComponentsBuilder
            .fromUriString(baseUrl)
            .queryParam("serviceKey", serviceKey)
            .queryParam("dataType", "JSON")
            .queryParam("srchStartNoftOcrnYmd", formatted)
            .queryParam("srchEndNoftOcrnYmd", formatted)
            .build(true) // serviceKey 에 이미 인코딩된 값이 있으므로 재인코딩 방지
            .toUri()

        try {
            return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String::class.java)!!
        } catch (ex: Exception) {
            log.error("Failed to fetch subway alerts from {} for date {}", uri, date, ex)
            throw RuntimeException("지하철 알림 조회 실패", ex)
        }
    }
}
