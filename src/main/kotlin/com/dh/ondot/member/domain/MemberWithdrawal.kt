package com.dh.ondot.member.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "member_withdrawals")
class MemberWithdrawal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_withdrawal_id")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "withdrawal_reason_id", nullable = false)
    val withdrawalReasonId: Long,

    @Column(name = "custom_reason", columnDefinition = "TEXT")
    val customReason: String? = null,
) : BaseTimeEntity() {

    companion object {
        @JvmStatic
        fun create(memberId: Long, reasonId: Long, customReason: String?): MemberWithdrawal =
            MemberWithdrawal(
                memberId = memberId,
                withdrawalReasonId = reasonId,
                customReason = customReason,
            )
    }
}
