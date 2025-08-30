package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.application.command.CreateChoicesCommand;
import com.dh.ondot.member.core.exception.NotFoundAnswerException;
import com.dh.ondot.member.core.exception.NotFoundQuestionException;
import com.dh.ondot.member.domain.*;
import com.dh.ondot.member.domain.repository.AnswerRepository;
import com.dh.ondot.member.domain.repository.ChoiceRepository;
import com.dh.ondot.member.domain.repository.QuestionRepository;
import com.dh.ondot.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChoiceService 테스트")
class ChoiceServiceTest {

    @Mock
    private ChoiceRepository choiceRepository;
    
    @Mock
    private QuestionRepository questionRepository;
    
    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private ChoiceService choiceService;

    @Test
    @DisplayName("회원의 선택사항들을 생성한다")
    void createChoices_ValidInput_CreatesChoices() {
        // given
        Member member = MemberFixture.defaultMember();
        
        Question question1 = Question.builder().id(1L).content("질문1").build();
        Question question2 = Question.builder().id(2L).content("질문2").build();
        Answer answer1 = Answer.builder().id(1L).content("답변1").question(question1).build();
        Answer answer2 = Answer.builder().id(2L).content("답변2").question(question2).build();
        
        List<CreateChoicesCommand.QuestionAnswerPair> pairs = Arrays.asList(
                new CreateChoicesCommand.QuestionAnswerPair(1L, 1L),
                new CreateChoicesCommand.QuestionAnswerPair(2L, 2L)
        );
        CreateChoicesCommand command = new CreateChoicesCommand(pairs);
        
        Choice choice1 = Choice.createChoice(member, question1, answer1);
        Choice choice2 = Choice.createChoice(member, question2, answer2);
        List<Choice> expectedChoices = Arrays.asList(choice1, choice2);
        
        given(questionRepository.findById(1L)).willReturn(Optional.of(question1));
        given(questionRepository.findById(2L)).willReturn(Optional.of(question2));
        given(answerRepository.findById(1L)).willReturn(Optional.of(answer1));
        given(answerRepository.findById(2L)).willReturn(Optional.of(answer2));
        given(choiceRepository.saveAll(anyList())).willReturn(expectedChoices);

        // when
        List<Choice> result = choiceService.createChoices(member, command);

        // then
        assertThat(result).hasSize(2);
        verify(questionRepository).findById(1L);
        verify(questionRepository).findById(2L);
        verify(answerRepository).findById(1L);
        verify(answerRepository).findById(2L);
        verify(choiceRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("존재하지 않는 질문 ID로 선택사항 생성 시 예외가 발생한다")
    void createChoices_InvalidQuestionId_ThrowsException() {
        // given
        Member member = MemberFixture.defaultMember();
        List<CreateChoicesCommand.QuestionAnswerPair> pairs = Arrays.asList(
                new CreateChoicesCommand.QuestionAnswerPair(999L, 1L)
        );
        CreateChoicesCommand command = new CreateChoicesCommand(pairs);
        
        given(questionRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> choiceService.createChoices(member, command))
                .isInstanceOf(NotFoundQuestionException.class);
        
        verify(questionRepository).findById(999L);
    }

    @Test
    @DisplayName("존재하지 않는 답변 ID로 선택사항 생성 시 예외가 발생한다")
    void createChoices_InvalidAnswerId_ThrowsException() {
        // given
        Member member = MemberFixture.defaultMember();
        Question question = Question.builder().id(1L).content("질문1").build();
        List<CreateChoicesCommand.QuestionAnswerPair> pairs = Arrays.asList(
                new CreateChoicesCommand.QuestionAnswerPair(1L, 999L)
        );
        CreateChoicesCommand command = new CreateChoicesCommand(pairs);
        
        given(questionRepository.findById(1L)).willReturn(Optional.of(question));
        given(answerRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> choiceService.createChoices(member, command))
                .isInstanceOf(NotFoundAnswerException.class);
        
        verify(questionRepository).findById(1L);
        verify(answerRepository).findById(999L);
    }

    @Test
    @DisplayName("회원 ID로 모든 선택사항을 삭제한다")
    void deleteAllByMemberId_ValidMemberId_DeletesChoices() {
        // given
        Long memberId = 1L;

        // when
        choiceService.deleteAllByMemberId(memberId);

        // then
        verify(choiceRepository).deleteByMemberId(memberId);
    }
}
