package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.OdsayUsage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

interface OdsayUsageRepository : JpaRepository<OdsayUsage, Long> {

    fun findByUsageDate(usageDate: LocalDate): Optional<OdsayUsage>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE OdsayUsage o
        SET o.count = o.count + 1
        WHERE o.usageDate = :usageDate
            AND o.count < 1000
        """
    )
    fun incrementUsageCount(@Param("usageDate") usageDate: LocalDate): Int

    @Query("SELECT o.count FROM OdsayUsage o WHERE o.usageDate = :usageDate")
    fun findUsageCountByDate(@Param("usageDate") usageDate: LocalDate): Optional<Int>
}
