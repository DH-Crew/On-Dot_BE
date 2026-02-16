package com.dh.ondot.member.domain.service

import com.dh.ondot.member.domain.MemberWithdrawal
import com.dh.ondot.member.domain.repository.MemberWithdrawalRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@DisplayName("WithdrawalService 테스트")
class WithdrawalServiceTest {

    @Mock
    private lateinit var withdrawalRepository: MemberWithdrawalRepository

    @InjectMocks
    private lateinit var withdrawalService: WithdrawalService

    @Test
    @DisplayName("탈퇴 사유를 저장한다")
    fun saveWithdrawalReason_ValidInput_SavesWithdrawal() {
        // given
        val memberId = 1L
        val withdrawalReasonId = 2L
        val customReason = "서비스 불만족"

        // when
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason)

        // then
        verify(withdrawalRepository).save(any(MemberWithdrawal::class.java))
    }

    @Test
    @DisplayName("커스텀 사유 없이 탈퇴 사유를 저장한다")
    fun saveWithdrawalReason_NoCustomReason_SavesWithdrawal() {
        // given
        val memberId = 1L
        val withdrawalReasonId = 1L
        val customReason: String? = null

        // when
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason)

        // then
        verify(withdrawalRepository).save(any(MemberWithdrawal::class.java))
    }

    @Test
    @DisplayName("빈 커스텀 사유로 탈퇴 사유를 저장한다")
    fun saveWithdrawalReason_EmptyCustomReason_SavesWithdrawal() {
        // given
        val memberId = 1L
        val withdrawalReasonId = 3L
        val customReason = ""

        // when
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason)

        // then
        verify(withdrawalRepository).save(any(MemberWithdrawal::class.java))
    }
}
