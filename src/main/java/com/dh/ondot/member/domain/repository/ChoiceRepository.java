package com.dh.ondot.member.domain.repository;

import com.dh.ondot.member.domain.Choice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    void deleteByMemberId(Long memberId);
}
