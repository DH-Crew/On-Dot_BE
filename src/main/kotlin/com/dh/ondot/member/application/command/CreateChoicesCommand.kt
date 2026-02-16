package com.dh.ondot.member.application.command

import com.dh.ondot.member.presentation.request.OnboardingRequest

data class CreateChoicesCommand(
    val questionAnswerPairs: List<QuestionAnswerPair>,
) {
    data class QuestionAnswerPair(
        val questionId: Long,
        val answerId: Long,
    )

    companion object {
        fun from(request: OnboardingRequest): CreateChoicesCommand {
            val pairs = request.questions.map { q ->
                QuestionAnswerPair(q.questionId, q.answerId)
            }
            return CreateChoicesCommand(pairs)
        }
    }
}
