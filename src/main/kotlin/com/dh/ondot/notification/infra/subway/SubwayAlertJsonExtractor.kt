package com.dh.ondot.notification.infra.subway

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class SubwayAlertJsonExtractor(
    private val objectMapper: ObjectMapper,
) {

    fun extractAlerts(rawJson: String): JsonNode {
        try {
            val root = objectMapper.readTree(rawJson)
            return root.path("response")
                .path("body")
                .path("items")
                .path("item")
        } catch (ex: JsonProcessingException) {
            throw IllegalStateException("Subway alert JSON 파싱 실패", ex)
        }
    }
}
