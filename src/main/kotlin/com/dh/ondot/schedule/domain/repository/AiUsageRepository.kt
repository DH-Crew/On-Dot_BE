package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.AiUsage
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional

interface AiUsageRepository : JpaRepository<AiUsage, Long> {
    fun findByMemberIdAndUsageDate(memberId: Long, usageDate: LocalDate): Optional<AiUsage>
}
