package com.dh.ondot.member.application;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.api.response.OnboardingResponse;
import com.dh.ondot.member.application.command.*;
import com.dh.ondot.member.application.dto.Token;
import com.dh.ondot.member.domain.*;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.event.UserRegistrationEvent;
import com.dh.ondot.member.domain.service.*;
import com.dh.ondot.schedule.domain.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberFacade {
    private final TokenFacade tokenFacade;
    private final MemberService memberService;
    private final AddressService addressService;
    private final ChoiceService choiceService;
    private final ScheduleService scheduleService;
    private final WithdrawalService withdrawalService;
    private final ApplicationEventPublisher eventPublisher;

    public Member getMember(Long memberId) {
        return memberService.getMemberIfExists(memberId);
    }

    @Transactional(readOnly = true)
    public Address getHomeAddress(Long memberId) {
        memberService.getMemberIfExists(memberId);
        return addressService.getHomeAddress(memberId);
    }

    @Transactional
    public OnboardingResponse onboarding(Long memberId, String mobileType, OnboardingRequest request) {
        Member member = memberService.getAndValidateAlreadyOnboarded(memberId);

        OnboardingCommand onboardingCommand = OnboardingCommand.from(request);
        CreateAddressCommand addressCommand = CreateAddressCommand.from(request);
        CreateChoicesCommand choicesCommand = CreateChoicesCommand.from(request);

        memberService.updateOnboardingInfo(member, onboardingCommand);
        addressService.createHomeAddress(member, addressCommand);
        choiceService.createChoices(member, choicesCommand);

        Token token = tokenFacade.issue(member.getId());

        publishUserRegistrationEvent(member, member.getOauthInfo().getOauthProvider(), mobileType);

        return OnboardingResponse.from(token.accessToken(), token.refreshToken(), member);
    }

    private void publishUserRegistrationEvent(Member member, OauthProvider oauthProvider, String mobileType) {
        Long totalMemberCount = memberService.getTotalMemberCount();

        UserRegistrationEvent event = new UserRegistrationEvent(
                member.getId(),
                member.getEmail(),
                oauthProvider,
                totalMemberCount,
                mobileType
        );

        eventPublisher.publishEvent(event);
    }

    @Transactional
    public Member updateMapProvider(Long memberId, String mapProvider) {
        Member member = memberService.getMemberIfExists(memberId);
        member.updateMapProvider(mapProvider);

        return member;
    }

    @Transactional
    public Address updateHomeAddress(
            Long memberId, String roadAddress,
            double longitude, double latitude
    ) {
        memberService.getMemberIfExists(memberId);
        CreateAddressCommand command = new CreateAddressCommand(roadAddress, longitude, latitude);
        return addressService.updateHomeAddress(memberId, command);
    }

    public Member updatePreparationTime(Long memberId, Integer preparationTime) {
        return memberService.updatePreparationTime(memberId, preparationTime);
    }

    @Transactional
    public void deleteMember(Long memberId, Long withdrawalReasonId, String customReason) {
        memberService.getMemberIfExists(memberId);
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason);

        scheduleService.deleteAllByMemberId(memberId);
        addressService.deleteAllByMemberId(memberId);
        choiceService.deleteAllByMemberId(memberId);
        memberService.deleteMember(memberId);
    }
}
