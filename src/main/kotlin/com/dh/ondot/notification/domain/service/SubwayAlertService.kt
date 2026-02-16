package com.dh.ondot.notification.domain.service

import com.dh.ondot.notification.domain.dto.SubwayAlertDto
import com.dh.ondot.notification.infra.subway.SubwayAlertApi
import com.dh.ondot.notification.infra.subway.SubwayAlertDtoMapper
import com.dh.ondot.notification.infra.subway.SubwayAlertJsonExtractor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SubwayAlertService(
    private val subwayAlertApi: SubwayAlertApi,
    private val subwayAlertJsonExtractor: SubwayAlertJsonExtractor,
    private val subwayAlertDtoMapper: SubwayAlertDtoMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun fetchAlertsByDate(date: LocalDate): List<SubwayAlertDto> {
        val rawJson = subwayAlertApi.getRawAlertsByDate(date)
        val alertsNode = subwayAlertJsonExtractor.extractAlerts(rawJson)

        if (!alertsNode.isArray) {
            return emptyList()
        }

        val list = mutableListOf<SubwayAlertDto>()
        for (node in alertsNode) {
            try {
                list.add(subwayAlertDtoMapper.toDto(node))
            } catch (ex: Exception) {
                log.warn("Failed to toDto subway alert JSON node -> DTO, node={}", node.toString(), ex)
            }
        }
        return list
    }
}
