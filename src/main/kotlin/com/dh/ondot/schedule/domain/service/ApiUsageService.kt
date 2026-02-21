package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.core.exception.MaxApiUsageLimitExceededException
import com.dh.ondot.schedule.domain.ApiUsage
import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.repository.ApiUsageRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class ApiUsageService(
    private val apiUsageRepository: ApiUsageRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun checkAndIncrementUsage(apiType: ApiType) {
        val today = TimeUtils.nowSeoulDate()
        val updatedRows = apiUsageRepository.incrementUsageCount(apiType, today)

        if (updatedRows == 0) {
            try {
                apiUsageRepository.save(ApiUsage.newForToday(apiType, today))
            } catch (e: DataIntegrityViolationException) {
                log.warn("ApiUsage 동시 삽입 충돌 발생 (apiType={}, date={}): {}", apiType, today, e.message)
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

    fun getUsageCount(apiType: ApiType, date: LocalDate): Int =
        apiUsageRepository.findUsageCountByDate(apiType, date).orElse(0)
}
