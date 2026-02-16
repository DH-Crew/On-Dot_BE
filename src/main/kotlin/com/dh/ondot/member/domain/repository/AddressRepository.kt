package com.dh.ondot.member.domain.repository

import com.dh.ondot.member.domain.Address
import com.dh.ondot.member.domain.enums.AddressType
import org.springframework.data.jpa.repository.JpaRepository

interface AddressRepository : JpaRepository<Address, Long> {
    fun findByMemberIdAndType(memberId: Long, type: AddressType): Address?
    fun deleteByMemberId(memberId: Long)
}
