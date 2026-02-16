package com.dh.ondot.notification.infra

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.notification.domain.service.AlertService
import com.dh.ondot.notification.domain.service.EmergencyAlertService
import com.dh.ondot.notification.domain.service.SubwayAlertService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PublicAlertBatchJob(
    private val alertService: AlertService,
    private val subwayAlertService: SubwayAlertService,
    private val emergencyAlertService: EmergencyAlertService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val EVERY_20_MINUTES = "0 0/20 * * * *"
    }

//    @Scheduled(cron = EVERY_20_MINUTES)
    fun refreshPublicAlerts() {
        val today = TimeUtils.nowSeoulDate()
        try {
            val subwayAlertDtoList = subwayAlertService.fetchAlertsByDate(today)
            alertService.saveSubwayAlerts(today, subwayAlertDtoList)
            val emergencyAlertDtoList = emergencyAlertService.fetchAlertsByDate(today)
            alertService.saveEmergencyAlerts(today, emergencyAlertDtoList)
        } catch (e: Exception) {
            log.error("Failed to update public alert data", e)
        }
    }
}
