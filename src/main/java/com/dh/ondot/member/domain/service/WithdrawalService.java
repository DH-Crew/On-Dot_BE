package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.domain.MemberWithdrawal;
import com.dh.ondot.member.domain.repository.MemberWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawalService {
    private final MemberWithdrawalRepository withdrawalRepository;

    public void withdrawMember(Long memberId, Long withdrawalReasonId, String customReason) {
        MemberWithdrawal withdrawal = MemberWithdrawal.create(
                memberId,
                withdrawalReasonId,
                customReason
        );
        withdrawalRepository.save(withdrawal);
    }
}
