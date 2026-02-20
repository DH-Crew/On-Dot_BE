package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.core.exception.MaxApiUsageLimitExceededException
import com.dh.ondot.schedule.domain.ApiUsage
import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.repository.ApiUsageRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ApiUsageService(
    private val apiUsageRepository: ApiUsageRepository,
) {
    @Transactional
    fun checkAndIncrementUsage(apiType: ApiType) {
        val today = TimeUtils.nowSeoulDate()
        val updatedRows = apiUsageRepository.incrementUsageCount(apiType, today)

        if (updatedRows == 0) {
            try {
                apiUsageRepository.save(ApiUsage.newForToday(apiType, today))
            } catch (e: DataIntegrityViolationException) {
                val retried = apiUsageRepository.incrementUsageCount(apiType, today)
                if (retried == 0) {
                    throw MaxApiUsageLimitExceededException(apiType.name, today)
                }
            }
        }
    }

    fun getRemainingUsageToday(apiType: ApiType): Int {
        val today = TimeUtils.nowSeoulDate()
        return apiUsageRepository.findUsageCountByDate(apiType, today)
            .map { count -> maxOf(0, ApiUsage.DAILY_LIMIT - count) }
            .orElse(ApiUsage.DAILY_LIMIT)
    }

    fun getUsageCount(apiType: ApiType, date: java.time.LocalDate): Int =
        apiUsageRepository.findUsageCountByDate(apiType, date).orElse(0)
}
