package com.dh.ondot.member.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "questions")
class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    val id: Long = 0L,

    @Column(name = "content", nullable = false)
    val content: String,
) : BaseTimeEntity()
