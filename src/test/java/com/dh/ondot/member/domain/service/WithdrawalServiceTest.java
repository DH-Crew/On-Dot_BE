package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.domain.MemberWithdrawal;
import com.dh.ondot.member.domain.repository.MemberWithdrawalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalService 테스트")
class WithdrawalServiceTest {

    @Mock
    private MemberWithdrawalRepository withdrawalRepository;

    @InjectMocks
    private WithdrawalService withdrawalService;

    @Test
    @DisplayName("탈퇴 사유를 저장한다")
    void saveWithdrawalReason_ValidInput_SavesWithdrawal() {
        // given
        Long memberId = 1L;
        Long withdrawalReasonId = 2L;
        String customReason = "서비스 불만족";

        // when
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason);

        // then
        verify(withdrawalRepository).save(any(MemberWithdrawal.class));
    }

    @Test
    @DisplayName("커스텀 사유 없이 탈퇴 사유를 저장한다")
    void saveWithdrawalReason_NoCustomReason_SavesWithdrawal() {
        // given
        Long memberId = 1L;
        Long withdrawalReasonId = 1L;
        String customReason = null;

        // when
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason);

        // then
        verify(withdrawalRepository).save(any(MemberWithdrawal.class));
    }

    @Test
    @DisplayName("빈 커스텀 사유로 탈퇴 사유를 저장한다")
    void saveWithdrawalReason_EmptyCustomReason_SavesWithdrawal() {
        // given
        Long memberId = 1L;
        Long withdrawalReasonId = 3L;
        String customReason = "";

        // when
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason);

        // then
        verify(withdrawalRepository).save(any(MemberWithdrawal.class));
    }
}
