package com.dh.ondot.notification.infra.emergency

import com.dh.ondot.notification.domain.dto.EmergencyAlertDto
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class EmergencyAlertDtoMapper {

    companion object {
        private val ISSUED_AT_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    }

    fun toDto(bodyNode: JsonNode): List<EmergencyAlertDto> {
        if (!bodyNode.isArray) {
            return emptyList()
        }
        val list = ArrayList<EmergencyAlertDto>(bodyNode.size())
        for (node in bodyNode) {
            val content = node.path("MSG_CN").asText("")
            val region = node.path("RCPTN_RGN_NM").asText("")
            val crtDt = node.path("CRT_DT").asText("")
            val issuedAt = LocalDateTime.parse(crtDt, ISSUED_AT_FMT)
            list.add(EmergencyAlertDto(content, region, issuedAt))
        }
        return list
    }
}
