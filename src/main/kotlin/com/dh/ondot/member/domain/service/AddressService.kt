package com.dh.ondot.member.domain.service

import com.dh.ondot.member.application.command.CreateAddressCommand
import com.dh.ondot.member.core.exception.NotFoundHomeAddressException
import com.dh.ondot.member.domain.Address
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.AddressType
import com.dh.ondot.member.domain.repository.AddressRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AddressService(
    private val addressRepository: AddressRepository,
) {
    @Transactional
    fun createHomeAddress(member: Member, command: CreateAddressCommand): Address {
        val address = Address.createByOnboarding(
            member,
            command.roadAddress,
            command.longitude,
            command.latitude,
        )
        return addressRepository.save(address)
    }

    fun getHomeAddress(memberId: Long): Address =
        addressRepository.findByMemberIdAndType(memberId, AddressType.HOME)
            ?: throw NotFoundHomeAddressException(memberId)

    @Transactional
    fun updateHomeAddress(memberId: Long, command: CreateAddressCommand): Address {
        val address = getHomeAddress(memberId)
        address.update(command.roadAddress, command.longitude, command.latitude)
        return address
    }

    @Transactional
    fun deleteAllByMemberId(memberId: Long) {
        addressRepository.deleteByMemberId(memberId)
    }
}
