package com.dh.ondot.member.domain.repository;

import com.dh.ondot.member.domain.Address;
import com.dh.ondot.member.domain.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByMemberIdAndType(Long memberId, AddressType type);
    void deleteByMemberId(Long memberId);
}
