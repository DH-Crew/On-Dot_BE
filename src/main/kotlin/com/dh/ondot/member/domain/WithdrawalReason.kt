package com.dh.ondot.member.domain

import jakarta.persistence.*

@Entity
@Table(name = "withdrawal_reasons")
class WithdrawalReason(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdrawal_reason_id")
    val id: Long = 0L,

    @Column(name = "reason", nullable = false)
    val reason: String,
)
