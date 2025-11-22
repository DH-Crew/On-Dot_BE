package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.application.command.CreateAddressCommand;
import com.dh.ondot.member.core.exception.NotFoundHomeAddressException;
import com.dh.ondot.member.domain.Address;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.enums.AddressType;
import com.dh.ondot.member.domain.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {
    private final AddressRepository addressRepository;

    @Transactional
    public Address createHomeAddress(Member member, CreateAddressCommand command) {
        Address address = Address.createByOnboarding(
                member, 
                command.roadAddress(), 
                command.longitude(), 
                command.latitude()
        );
        return addressRepository.save(address);
    }

    public Address getHomeAddress(Long memberId) {
        return addressRepository.findByMemberIdAndType(memberId, AddressType.HOME)
                .orElseThrow(() -> new NotFoundHomeAddressException(memberId));
    }

    @Transactional
    public Address updateHomeAddress(Long memberId, CreateAddressCommand command) {
        Address address = getHomeAddress(memberId);
        address.update(command.roadAddress(), command.longitude(), command.latitude());
        return address;
    }

    @Transactional
    public void deleteAllByMemberId(Long memberId) {
        addressRepository.deleteByMemberId(memberId);
    }
}
