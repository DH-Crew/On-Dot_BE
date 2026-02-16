package com.dh.ondot.notification.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.notification.domain.dto.EmergencyAlertDto
import com.dh.ondot.notification.domain.repository.EmergencyAlertRepository
import com.dh.ondot.notification.infra.emergency.EmergencyAlertApi
import com.dh.ondot.notification.infra.emergency.EmergencyAlertDtoMapper
import com.dh.ondot.notification.infra.emergency.EmergencyAlertJsonExtractor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class EmergencyAlertService(
    private val emergencyAlertApi: EmergencyAlertApi,
    private val emergencyAlertJsonExtractor: EmergencyAlertJsonExtractor,
    private val emergencyAlertDtoMapper: EmergencyAlertDtoMapper,
    private val emergencyAlertRepository: EmergencyAlertRepository,
) {

    fun fetchAlertsByDate(date: LocalDate): List<EmergencyAlertDto> {
        val rawJson = emergencyAlertApi.fetchAlertsByDate(date)
        val alertsNode = emergencyAlertJsonExtractor.extractAlerts(rawJson)
        return emergencyAlertDtoMapper.toDto(alertsNode)
    }

    @Transactional(readOnly = true)
    fun getIssuesByAddress(roadAddress: String): String {
        val regionKey = extractRegionKey(roadAddress)
        val provinceKey = extractProvince(roadAddress)
        val allRegion = "$provinceKey 전체"

        val today = TimeUtils.nowSeoulDate()
        val from = TimeUtils.toInstant(today.atStartOfDay())
        val to = TimeUtils.toInstant(today.plusDays(1).atStartOfDay())
        val alerts = emergencyAlertRepository.findAllByCreatedAtBetween(from, to)

        val contents = alerts
            .filter { a -> matchesRegionCSV(a.regionName, regionKey, allRegion) }
            .map { it.content }

        return contents.joinToString("\n")
    }

    // "서울특별시 동작구 흑석로23-2" → "서울특별시 동작구"
    private fun extractRegionKey(roadAddress: String): String {
        val tok = roadAddress.split("\\s+".toRegex())
        return when {
            tok.size >= 2 -> "${tok[0]} ${tok[1]}"
            tok.size == 1 -> tok[0]
            else -> ""
        }
    }

    // "서울특별시 동작구 흑석로23-2" → "서울특별시"
    private fun extractProvince(roadAddress: String): String {
        val tok = roadAddress.split("\\s+".toRegex())
        return if (tok.isNotEmpty()) tok[0] else ""
    }

    private fun matchesRegionCSV(regionCsv: String, regionKey: String, allRegion: String): Boolean {
        for (part in regionCsv.split(",")) {
            val r = part.trim()
            if (r == regionKey || r == allRegion) {
                return true
            }
        }
        return false
    }
}
