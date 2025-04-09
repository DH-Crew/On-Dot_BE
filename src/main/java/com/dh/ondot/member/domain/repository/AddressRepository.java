package com.dh.ondot.member.domain.repository;

import com.dh.ondot.member.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
