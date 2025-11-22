package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.domain.AiUsage;
import com.dh.ondot.schedule.domain.repository.AiUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AiUsageService {
    private final AiUsageRepository repo;

    /** 호출 1회당 사용량 +1 */
    public void increaseUsage(Long memberId) {
        LocalDate today = TimeUtils.nowSeoulDate();

        repo.findByMemberIdAndUsageDate(memberId, today)
                .ifPresentOrElse(AiUsage::increase,
                        () -> repo.save(AiUsage.newForToday(memberId, today)));
    }
}
