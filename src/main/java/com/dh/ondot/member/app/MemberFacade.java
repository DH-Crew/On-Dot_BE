package com.dh.ondot.member.app;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.core.exception.NotFoundAddressException;
import com.dh.ondot.member.core.exception.NotFoundAnswerException;
import com.dh.ondot.member.core.exception.NotFoundQuestionException;
import com.dh.ondot.member.domain.*;
import com.dh.ondot.member.domain.enums.AddressType;
import com.dh.ondot.member.domain.repository.*;
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
    private final ChoiceRepository choiceRepository;

    @Transactional(readOnly = true)
    public Address getHomeAddress(Long memberId) {
        memberService.findExistingMember(memberId);
        return addressRepository.findByMemberIdAndType(memberId, AddressType.HOME)
                .orElseThrow(() -> new NotFoundAddressException(memberId));
    }

    @Transactional
    public Member onboarding(Long memberId, OnboardingRequest request) {
        Member member = memberService.findExistingMember(memberId);
        member.updateOnboarding(request.preparationTime(), request.soundCategory(), request.ringTone(), request.volume());

        Address address = Address.createByOnboarding(member, request.roadAddress(), request.longitude(), request.latitude());
        addressRepository.save(address);

        List<Choice> choiceList = new ArrayList<>();
        for(OnboardingRequest.QuestionDto questionDto : request.questions()) {
            Question question = questionRepository.findById(questionDto.questionId())
                    .orElseThrow(() -> new NotFoundQuestionException(questionDto.questionId()));
            Answer answer = answerRepository.findById(questionDto.answerId())
                    .orElseThrow(() -> new NotFoundAnswerException(questionDto.answerId()));

            Choice choice = Choice.createChoice(member, question, answer);
            choiceList.add(choice);
        }
        choiceRepository.saveAll(choiceList);

        return member;
    }

    @Transactional
    public Member updateMapProvider(Long memberId, String mapProvider) {
        Member member = memberService.findExistingMember(memberId);
        member.updateMapProvider(mapProvider);

        return member;
    }

    @Transactional
    public Address updateHomeAddress(Long memberId, String roadAddress,
                                     double longitude, double latitude) {
        memberService.findExistingMember(memberId);

        Address address = addressRepository.findByMemberIdAndType(memberId, AddressType.HOME)
                .orElseThrow(() -> new NotFoundAddressException(memberId));

        address.update(roadAddress, longitude, latitude);

        return address;
    }

    @Transactional
    public void deleteMember(Long memberId) {
        memberService.findExistingMember(memberId);
        memberRepository.deleteById(memberId);
    }
}
