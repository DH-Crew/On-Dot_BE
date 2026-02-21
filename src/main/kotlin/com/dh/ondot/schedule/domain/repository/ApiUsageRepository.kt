package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.ApiUsage
import com.dh.ondot.schedule.domain.enums.ApiType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

interface ApiUsageRepository : JpaRepository<ApiUsage, Long> {

    fun findByApiTypeAndUsageDate(apiType: ApiType, usageDate: LocalDate): Optional<ApiUsage>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE ApiUsage a
        SET a.count = a.count + 1
        WHERE a.apiType = :apiType
            AND a.usageDate = :usageDate
            AND a.count < 1000
        """
    )
    fun incrementUsageCount(@Param("apiType") apiType: ApiType, @Param("usageDate") usageDate: LocalDate): Int

    @Query("SELECT a.count FROM ApiUsage a WHERE a.apiType = :apiType AND a.usageDate = :usageDate")
    fun findUsageCountByDate(@Param("apiType") apiType: ApiType, @Param("usageDate") usageDate: LocalDate): Optional<Int>
}
