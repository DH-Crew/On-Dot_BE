package com.dh.ondot.member.application;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.api.response.OnboardingResponse;
import com.dh.ondot.member.application.dto.Token;
import com.dh.ondot.member.core.exception.NotFoundAddressException;
import com.dh.ondot.member.core.exception.NotFoundAnswerException;
import com.dh.ondot.member.core.exception.NotFoundQuestionException;
import com.dh.ondot.member.domain.*;
import com.dh.ondot.member.domain.enums.AddressType;
import com.dh.ondot.member.domain.repository.*;
import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.member.domain.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberFacade {
    private final TokenFacade tokenFacade;
    private final MemberService memberService;
    private final WithdrawalService withdrawalService;
    private final AddressRepository addressRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ChoiceRepository choiceRepository;

    @Transactional
    public void deactivateMember(Long memberId, Long withdrawalReasonId, String customReason) {
        Member member = memberService.getMemberIfExists(memberId);
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason);
        member.deactivate();
    }

    public Member getMember(Long memberId) {
        return memberService.getMemberIfExists(memberId);
    }

    @Transactional(readOnly = true)
    public Address getHomeAddress(Long memberId) {
        memberService.getMemberIfExists(memberId);
        return addressRepository.findByMemberIdAndType(memberId, AddressType.HOME)
                .orElseThrow(() -> new NotFoundAddressException(memberId));
    }

    @Transactional
    public OnboardingResponse onboarding(Long memberId, OnboardingRequest request) {
        Member member = memberService.getMemberIfExists(memberId);
        member.updateOnboarding(
                request.preparationTime(), request.alarmMode(),
                request.isSnoozeEnabled(), request.snoozeInterval(), request.snoozeCount(),
                request.soundCategory(), request.ringTone(), request.volume()
        );

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

        Token token = tokenFacade.issue(member.getId());

        return OnboardingResponse.from(token.accessToken(), token.refreshToken(), member);
    }

    @Transactional
    public Member updateMapProvider(Long memberId, String mapProvider) {
        Member member = memberService.getMemberIfExists(memberId);
        member.updateMapProvider(mapProvider);

        return member;
    }

    @Transactional
    public Address updateHomeAddress(Long memberId, String roadAddress,
                                     double longitude, double latitude) {
        memberService.getMemberIfExists(memberId);

        Address address = addressRepository.findByMemberIdAndType(memberId, AddressType.HOME)
                .orElseThrow(() -> new NotFoundAddressException(memberId));

        address.update(roadAddress, longitude, latitude);

        return address;
    }

    public Member updatePreparationTime(Long memberId, Integer preparationTime) {
        return memberService.updatePreparationTime(memberId, preparationTime);
    }
}
