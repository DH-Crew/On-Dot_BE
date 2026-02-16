package com.dh.ondot.member.domain.repository

import com.dh.ondot.member.domain.Choice
import org.springframework.data.jpa.repository.JpaRepository

interface ChoiceRepository : JpaRepository<Choice, Long> {
    fun deleteByMemberId(memberId: Long)
}
