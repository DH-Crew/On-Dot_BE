package com.dh.ondot.member.app;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.core.exception.NotFoundQuestionException;
import com.dh.ondot.member.domain.Address;
import com.dh.ondot.member.domain.Answer;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.Question;
import com.dh.ondot.member.domain.repository.AddressRepository;
import com.dh.ondot.member.domain.repository.AnswerRepository;
import com.dh.ondot.member.domain.repository.MemberRepository;
import com.dh.ondot.member.domain.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.dh.ondot.member.app.MemberServiceHelper.findExistingMember;

@Service
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public Member onboarding(Long memberId, OnboardingRequest request) {
        Member member = findExistingMember(memberRepository, memberId);
        member.updateOnboarding(request.preparationTime(), request.ringTone(), request.volume());

        Address address = Address.createByOnboardingAddress(member, request.addressTitle(), request.longitude(), request.latitude());
        addressRepository.save(address);

        List<Answer> answerList = new ArrayList<>();
        for(OnboardingRequest.QuestionDto questionDto : request.questions()) {
            Question question = questionRepository.findById(questionDto.questionId())
                    .orElseThrow(() -> new NotFoundQuestionException(questionDto.questionId()));
            Answer answer = Answer.createByOnboarding(question, member, questionDto.answer());
            answerList.add(answer);
        }
        answerRepository.saveAll(answerList);

        return member;
    }

    @Transactional
    public void deleteMember(Long memberId) {
        findExistingMember(memberRepository, memberId);
        memberRepository.deleteById(memberId);
    }
}
