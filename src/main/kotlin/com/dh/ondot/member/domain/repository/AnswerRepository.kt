package com.dh.ondot.member.domain.repository

import com.dh.ondot.member.domain.Answer
import org.springframework.data.jpa.repository.JpaRepository

interface AnswerRepository : JpaRepository<Answer, Long>
