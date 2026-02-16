package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.core.exception.MaxOdsayUsageLimitExceededException
import com.dh.ondot.schedule.domain.OdsayUsage
import com.dh.ondot.schedule.domain.repository.OdsayUsageRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OdsayUsageService(
    private val odsayUsageRepository: OdsayUsageRepository,
) {
    @Transactional
    fun checkAndIncrementUsage() {
        val today = TimeUtils.nowSeoulDate()
        val updatedRows = odsayUsageRepository.incrementUsageCount(today)

        if (updatedRows == 0) {
            try {
                odsayUsageRepository.save(OdsayUsage.newForToday(today))
            } catch (e: DataIntegrityViolationException) {
                val retired = odsayUsageRepository.incrementUsageCount(today)
                if (retired == 0) {
                    throw MaxOdsayUsageLimitExceededException(today)
                }
            }
        }
    }

    fun getRemainingUsageToday(): Int {
        val today = TimeUtils.nowSeoulDate()

        return odsayUsageRepository.findUsageCountByDate(today)
            .map { count -> maxOf(0, 1000 - count) }
            .orElse(1000)
    }

    fun getUsageCount(date: java.time.LocalDate): Int =
        odsayUsageRepository.findUsageCountByDate(date).orElse(0)
}
