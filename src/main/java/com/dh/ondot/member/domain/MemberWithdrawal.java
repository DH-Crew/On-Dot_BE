package com.dh.ondot.member.domain;

import com.dh.ondot.core.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "member_withdrawals")
public class MemberWithdrawal extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_withdrawal_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "withdrawal_reason_id", nullable = false)
    private Long withdrawalReasonId;

    @Column(name = "custom_reason", columnDefinition = "TEXT")
    private String customReason;

    public static MemberWithdrawal create(Long memberId, Long reasonId, String customReason) {
        return MemberWithdrawal.builder()
                .memberId(memberId)
                .withdrawalReasonId(reasonId)
                .customReason(customReason)
                .build();
    }
}
