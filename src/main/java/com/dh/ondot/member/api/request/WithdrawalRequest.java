package com.dh.ondot.member.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WithdrawalRequest(
        @NotNull(message = "탈퇴 사유 ID는 필수입니다.")
        Long withdrawalReasonId,

        @Size(max = 300, message = "기타 사유는 300자 이내여야 합니다.")
        String customReason
) {
}
