package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.core.exception.MaxOdsayUsageLimitExceededException;
import com.dh.ondot.schedule.domain.OdsayUsage;
import com.dh.ondot.schedule.domain.repository.OdsayUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OdsayUsageService {
    private final OdsayUsageRepository odsayUsageRepository;

    @Transactional
    public void checkAndIncrementUsage() {
        LocalDate today = TimeUtils.nowSeoulDate();
        int updatedRows = odsayUsageRepository.incrementUsageCount(today);

        if (updatedRows == 0) {
            try {
                odsayUsageRepository.save(OdsayUsage.newForToday(today));
            } catch (DataIntegrityViolationException e) {
                int retired = odsayUsageRepository.incrementUsageCount(today);
                if (retired == 0) {
                    throw new MaxOdsayUsageLimitExceededException(today);
                }
            }
        }
    }

    public int getRemainingUsageToday() {
        LocalDate today = TimeUtils.nowSeoulDate();

        return odsayUsageRepository.findUsageCountByDate(today)
                .map(count -> Math.max(0, 1000 - count))
                .orElse(1000);
    }

    public int getUsageCount(LocalDate date) {
        return odsayUsageRepository.findUsageCountByDate(date).orElse(0);
    }
}
