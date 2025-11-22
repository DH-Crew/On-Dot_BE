package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.application.command.CreateChoicesCommand;
import com.dh.ondot.member.core.exception.NotFoundAnswerException;
import com.dh.ondot.member.core.exception.NotFoundQuestionException;
import com.dh.ondot.member.domain.*;
import com.dh.ondot.member.domain.repository.AnswerRepository;
import com.dh.ondot.member.domain.repository.ChoiceRepository;
import com.dh.ondot.member.domain.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChoiceService {
    private final ChoiceRepository choiceRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public List<Choice> createChoices(Member member, CreateChoicesCommand command) {
        List<Choice> choiceList = new ArrayList<>();
        
        for (CreateChoicesCommand.QuestionAnswerPair pair : command.questionAnswerPairs()) {
            Question question = questionRepository.findById(pair.questionId())
                    .orElseThrow(() -> new NotFoundQuestionException(pair.questionId()));
            Answer answer = answerRepository.findById(pair.answerId())
                    .orElseThrow(() -> new NotFoundAnswerException(pair.answerId()));

            Choice choice = Choice.createChoice(member, question, answer);
            choiceList.add(choice);
        }
        
        return choiceRepository.saveAll(choiceList);
    }

    @Transactional
    public void deleteAllByMemberId(Long memberId) {
        choiceRepository.deleteByMemberId(memberId);
    }
}
