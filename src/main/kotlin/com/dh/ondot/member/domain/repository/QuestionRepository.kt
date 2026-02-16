package com.dh.ondot.member.domain.repository

import com.dh.ondot.member.domain.Question
import org.springframework.data.jpa.repository.JpaRepository

interface QuestionRepository : JpaRepository<Question, Long>
