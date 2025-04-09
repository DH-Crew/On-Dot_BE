package com.dh.ondot.member.domain.repository;

import com.dh.ondot.member.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
