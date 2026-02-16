package com.dh.ondot.member.domain.service

import com.dh.ondot.member.application.command.CreateChoicesCommand
import com.dh.ondot.member.core.exception.NotFoundAnswerException
import com.dh.ondot.member.core.exception.NotFoundQuestionException
import com.dh.ondot.member.domain.Choice
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.repository.AnswerRepository
import com.dh.ondot.member.domain.repository.ChoiceRepository
import com.dh.ondot.member.domain.repository.QuestionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChoiceService(
    private val choiceRepository: ChoiceRepository,
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
) {
    @Transactional
    fun createChoices(member: Member, command: CreateChoicesCommand): List<Choice> {
        val choiceList = mutableListOf<Choice>()

        for (pair in command.questionAnswerPairs()) {
            val question = questionRepository.findById(pair.questionId())
                .orElseThrow { NotFoundQuestionException(pair.questionId()) }
            val answer = answerRepository.findById(pair.answerId())
                .orElseThrow { NotFoundAnswerException(pair.answerId()) }

            val choice = Choice.createChoice(member, question, answer)
            choiceList.add(choice)
        }

        return choiceRepository.saveAll(choiceList)
    }

    @Transactional
    fun deleteAllByMemberId(memberId: Long) {
        choiceRepository.deleteByMemberId(memberId)
    }
}
