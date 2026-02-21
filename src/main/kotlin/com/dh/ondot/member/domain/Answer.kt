package com.dh.ondot.member.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "answers")
class Answer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @Column(name = "content", nullable = false)
    val content: String,
) : BaseTimeEntity() {

    companion object {
        fun create(question: Question, content: String): Answer =
            Answer(question = question, content = content)
    }
}
