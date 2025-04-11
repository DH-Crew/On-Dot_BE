package com.dh.ondot.member.app;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.core.exception.NotFoundQuestionException;
import com.dh.ondot.member.domain.*;
import com.dh.ondot.member.domain.repository.AddressRepository;
import com.dh.ondot.member.domain.repository.AnswerRepository;
import com.dh.ondot.member.domain.repository.MemberRepository;
import com.dh.ondot.member.domain.repository.QuestionRepository;
import com.dh.ondot.member.domain.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public Member onboarding(Long memberId, OnboardingRequest request) {
        Member member = memberService.findExistingMember(memberId);
        member.updateOnboarding(request.preparationTime(), request.soundCategory(), request.ringTone(), request.volume());

        Address address = Address.createByOnboarding(member, request.addressTitle(), request.longitude(), request.latitude());
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
    public Member updateMapProvider(Long memberId, String mapProvider) {
        Member member = memberService.findExistingMember(memberId);
        member.updateMapProvider(mapProvider);

        return member;
    }

    @Transactional
    public void deleteMember(Long memberId) {
        memberService.findExistingMember(memberId);
        memberRepository.deleteById(memberId);
    }
}
