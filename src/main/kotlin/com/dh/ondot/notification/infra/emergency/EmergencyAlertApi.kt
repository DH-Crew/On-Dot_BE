package com.dh.ondot.notification.infra.emergency

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class EmergencyAlertApi {

    companion object {
        private val YYYYMMDD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    @Value("\${external-api.safety-data.base-url}")
    private lateinit var baseUrl: String

    @Value("\${external-api.safety-data.service-key}")
    private lateinit var serviceKey: String

    private lateinit var restClient: RestClient

    @PostConstruct
    fun init() {
        restClient = RestClient.create(baseUrl)
    }

    fun fetchAlertsByDate(date: LocalDate): String {
        val formattedDate = date.format(YYYYMMDD)
        return restClient.get()
            .uri { b ->
                b.queryParam("serviceKey", serviceKey)
                    .queryParam("crtDt", formattedDate)
                    .build()
            }
            .retrieve()
            .body(String::class.java)!!
    }
}
