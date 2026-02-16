package com.dh.ondot.member.application.command

data class CreateChoicesCommand(
    val questionAnswerPairs: List<QuestionAnswerPair>,
) {
    data class QuestionAnswerPair(
        val questionId: Long,
        val answerId: Long,
    )
}
