package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.AiUsage
import com.dh.ondot.schedule.domain.repository.AiUsageRepository
import org.springframework.stereotype.Service

@Service
class AiUsageService(
    private val repo: AiUsageRepository,
) {
    /** 호출 1회당 사용량 +1 */
    fun increaseUsage(memberId: Long) {
        val today = TimeUtils.nowSeoulDate()

        repo.findByMemberIdAndUsageDate(memberId, today)
            .ifPresentOrElse(
                { it.increase() },
                { repo.save(AiUsage.newForToday(memberId, today)) },
            )
    }
}
