package com.dh.ondot.member.domain.service

import com.dh.ondot.member.domain.MemberWithdrawal
import com.dh.ondot.member.domain.repository.MemberWithdrawalRepository
import org.springframework.stereotype.Service

@Service
class WithdrawalService(
    private val withdrawalRepository: MemberWithdrawalRepository,
) {
    fun saveWithdrawalReason(memberId: Long, withdrawalReasonId: Long, customReason: String?) {
        val withdrawal = MemberWithdrawal.create(
            memberId,
            withdrawalReasonId,
            customReason,
        )
        withdrawalRepository.save(withdrawal)
    }
}
