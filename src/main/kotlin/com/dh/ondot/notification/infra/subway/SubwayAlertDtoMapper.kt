package com.dh.ondot.notification.infra.subway

import com.dh.ondot.notification.domain.dto.SubwayAlertDto
import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.DateTimeException
import java.time.LocalDateTime

@Component
class SubwayAlertDtoMapper {

    private val log = LoggerFactory.getLogger(javaClass)

    fun toDto(node: JsonNode): SubwayAlertDto {
        val title = node.path("noftTtl").asText("")
        val content = node.path("noftCn").asText("")
        val lineName = node.path("lineNmLst").asText("")

        val notificationOccurTime = node.path("noftOcrnDt").asText("")
        val eventBeginAt = node.path("xcseSitnBgngDt").asText("")

        val createdAt: LocalDateTime
        val startAt: LocalDateTime
        try {
            createdAt = LocalDateTime.parse(notificationOccurTime)
            startAt = if (eventBeginAt.isBlank()) {
                createdAt
            } else {
                LocalDateTime.parse(eventBeginAt)
            }
        } catch (ex: DateTimeException) {
            log.error("Failed to parse datetime fields: begin={}, notif={}", eventBeginAt, notificationOccurTime, ex)
            throw IllegalArgumentException("Invalid datetime format in subway alert data", ex)
        }

        return SubwayAlertDto(title, content, lineName, startAt, createdAt)
    }
}
