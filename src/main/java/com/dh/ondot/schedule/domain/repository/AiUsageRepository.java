package com.dh.ondot.schedule.domain.repository;

import com.dh.ondot.schedule.domain.AiUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {
    Optional<AiUsage> findByMemberIdAndUsageDate(Long memberId, LocalDate usageDate);
}
