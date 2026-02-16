package com.dh.ondot.notification.domain.repository

import com.dh.ondot.notification.domain.EmergencyAlert
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface EmergencyAlertRepository : JpaRepository<EmergencyAlert, Long> {
    fun findAllByCreatedAtBetween(start: Instant, end: Instant): List<EmergencyAlert>
}
