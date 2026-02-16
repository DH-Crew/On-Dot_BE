package com.dh.ondot.notification.infra.emergency

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EmergencyAlertJsonExtractor(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun extractAlerts(rawJson: String): JsonNode {
        try {
            return objectMapper.readTree(rawJson)
                .path("body")
        } catch (e: JsonProcessingException) {
            log.error("[EmergencyAlertJsonExtractor] JSON parsing failed", e)
            throw IllegalStateException("[EmergencyAlertJsonExtractor] JSON parsing failed", e)
        }
    }
}
