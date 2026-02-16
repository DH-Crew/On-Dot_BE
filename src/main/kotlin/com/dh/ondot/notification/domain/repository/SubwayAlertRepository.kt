package com.dh.ondot.notification.domain.repository

import com.dh.ondot.notification.domain.SubwayAlert
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface SubwayAlertRepository : JpaRepository<SubwayAlert, Long> {
    fun findAllByCreatedAtBetween(start: Instant, end: Instant): List<SubwayAlert>
}
