package com.dh.ondot.notification.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.notification.domain.EmergencyAlert
import com.dh.ondot.notification.domain.SubwayAlert
import com.dh.ondot.notification.domain.dto.EmergencyAlertDto
import com.dh.ondot.notification.domain.dto.SubwayAlertDto
import com.dh.ondot.notification.domain.repository.EmergencyAlertRepository
import com.dh.ondot.notification.domain.repository.SubwayAlertRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AlertService(
    private val subwayAlertRepository: SubwayAlertRepository,
    private val emergencyAlertRepository: EmergencyAlertRepository,
) {

    @Transactional
    fun saveSubwayAlerts(date: LocalDate, dtos: List<SubwayAlertDto>) {
        if (dtos.isEmpty()) return

        val startOfDay = TimeUtils.toInstant(date.atStartOfDay())
        val nextDay = TimeUtils.toInstant(date.plusDays(1).atStartOfDay())

        val existing = subwayAlertRepository
            .findAllByCreatedAtBetween(startOfDay, nextDay)
            .map { it.createdAt }
            .toSet()

        val toSave = dtos
            .filter { dto -> !existing.contains(TimeUtils.toInstant(dto.createdAt)) }
            .map { dto ->
                SubwayAlert.create(
                    dto.title,
                    dto.content,
                    dto.lineName,
                    dto.startAt,
                    dto.createdAt,
                )
            }

        if (toSave.isNotEmpty()) {
            subwayAlertRepository.saveAll(toSave)
        }
    }

    @Transactional
    fun saveEmergencyAlerts(date: LocalDate, dtos: List<EmergencyAlertDto>) {
        if (dtos.isEmpty()) return

        val startOfDay = TimeUtils.toInstant(date.atStartOfDay())
        val nextDay = TimeUtils.toInstant(date.plusDays(1).atStartOfDay())

        val existing = emergencyAlertRepository
            .findAllByCreatedAtBetween(startOfDay, nextDay)
            .map { it.createdAt }
            .toSet()

        val toSave = dtos
            .filter { dto -> !existing.contains(TimeUtils.toInstant(dto.createdAt)) }
            .map { dto ->
                EmergencyAlert.create(
                    dto.content,
                    dto.regionName,
                    dto.createdAt,
                )
            }

        if (toSave.isNotEmpty()) {
            emergencyAlertRepository.saveAll(toSave)
        }
    }
}
