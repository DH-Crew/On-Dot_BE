package com.dh.ondot.member.domain.repository

import com.dh.ondot.member.domain.MemberWithdrawal
import org.springframework.data.jpa.repository.JpaRepository

interface MemberWithdrawalRepository : JpaRepository<MemberWithdrawal, Long>
