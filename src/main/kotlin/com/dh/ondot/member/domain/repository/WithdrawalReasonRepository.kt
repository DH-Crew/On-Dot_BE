package com.dh.ondot.member.domain.repository

import com.dh.ondot.member.domain.WithdrawalReason
import org.springframework.data.jpa.repository.JpaRepository

interface WithdrawalReasonRepository : JpaRepository<WithdrawalReason, Long>
