package com.dh.ondot.member.domain.service

import com.dh.ondot.member.application.command.CreateChoicesCommand
import com.dh.ondot.member.core.exception.NotFoundAnswerException
import com.dh.ondot.member.core.exception.NotFoundQuestionException
import com.dh.ondot.member.domain.Answer
import com.dh.ondot.member.domain.Choice
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.Question
import com.dh.ondot.member.domain.repository.AnswerRepository
import com.dh.ondot.member.domain.repository.ChoiceRepository
import com.dh.ondot.member.domain.repository.QuestionRepository
import com.dh.ondot.member.fixture.MemberFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("ChoiceService 테스트")
class ChoiceServiceTest {

    @Mock
    private lateinit var choiceRepository: ChoiceRepository

    @Mock
    private lateinit var questionRepository: QuestionRepository

    @Mock
    private lateinit var answerRepository: AnswerRepository

    @InjectMocks
    private lateinit var choiceService: ChoiceService

    @Test
    @DisplayName("회원의 선택사항들을 생성한다")
    fun createChoices_ValidInput_CreatesChoices() {
        // given
        val member = MemberFixture.defaultMember()

        val question1 = Question(1L, "질문1")
        val question2 = Question(2L, "질문2")
        val answer1 = Answer(1L, question1, "답변1")
        val answer2 = Answer(2L, question2, "답변2")

        val pairs = listOf(
            CreateChoicesCommand.QuestionAnswerPair(1L, 1L),
            CreateChoicesCommand.QuestionAnswerPair(2L, 2L),
        )
        val command = CreateChoicesCommand(pairs)

        val choice1 = Choice.createChoice(member, question1, answer1)
        val choice2 = Choice.createChoice(member, question2, answer2)
        val expectedChoices = listOf(choice1, choice2)

        given(questionRepository.findById(1L)).willReturn(Optional.of(question1))
        given(questionRepository.findById(2L)).willReturn(Optional.of(question2))
        given(answerRepository.findById(1L)).willReturn(Optional.of(answer1))
        given(answerRepository.findById(2L)).willReturn(Optional.of(answer2))
        given(choiceRepository.saveAll(anyList<Choice>())).willReturn(expectedChoices)

        // when
        val result = choiceService.createChoices(member, command)

        // then
        assertThat(result).hasSize(2)
        verify(questionRepository).findById(1L)
        verify(questionRepository).findById(2L)
        verify(answerRepository).findById(1L)
        verify(answerRepository).findById(2L)
        verify(choiceRepository).saveAll(anyList<Choice>())
    }

    @Test
    @DisplayName("존재하지 않는 질문 ID로 선택사항 생성 시 예외가 발생한다")
    fun createChoices_InvalidQuestionId_ThrowsException() {
        // given
        val member = MemberFixture.defaultMember()
        val pairs = listOf(
            CreateChoicesCommand.QuestionAnswerPair(999L, 1L),
        )
        val command = CreateChoicesCommand(pairs)

        given(questionRepository.findById(999L)).willReturn(Optional.empty())

        // when & then
        assertThatThrownBy { choiceService.createChoices(member, command) }
            .isInstanceOf(NotFoundQuestionException::class.java)

        verify(questionRepository).findById(999L)
    }

    @Test
    @DisplayName("존재하지 않는 답변 ID로 선택사항 생성 시 예외가 발생한다")
    fun createChoices_InvalidAnswerId_ThrowsException() {
        // given
        val member = MemberFixture.defaultMember()
        val question = Question(1L, "질문1")
        val pairs = listOf(
            CreateChoicesCommand.QuestionAnswerPair(1L, 999L),
        )
        val command = CreateChoicesCommand(pairs)

        given(questionRepository.findById(1L)).willReturn(Optional.of(question))
        given(answerRepository.findById(999L)).willReturn(Optional.empty())

        // when & then
        assertThatThrownBy { choiceService.createChoices(member, command) }
            .isInstanceOf(NotFoundAnswerException::class.java)

        verify(questionRepository).findById(1L)
        verify(answerRepository).findById(999L)
    }

    @Test
    @DisplayName("회원 ID로 모든 선택사항을 삭제한다")
    fun deleteAllByMemberId_ValidMemberId_DeletesChoices() {
        // given
        val memberId = 1L

        // when
        choiceService.deleteAllByMemberId(memberId)

        // then
        verify(choiceRepository).deleteByMemberId(memberId)
    }
}
