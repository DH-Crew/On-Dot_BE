package com.dh.ondot.schedule.domain.repository;

import com.dh.ondot.schedule.domain.OdsayUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface OdsayUsageRepository extends JpaRepository<OdsayUsage, Long> {

    Optional<OdsayUsage> findByUsageDate(LocalDate usageDate);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE OdsayUsage o
        SET o.count = o.count + 1
        WHERE o.usageDate = :usageDate
            AND o.count < 1000
    """)
    int incrementUsageCount(@Param("usageDate") LocalDate usageDate);

    @Query("SELECT o.count FROM OdsayUsage o WHERE o.usageDate = :usageDate")
    Optional<Integer> findUsageCountByDate(@Param("usageDate") LocalDate usageDate);
}
