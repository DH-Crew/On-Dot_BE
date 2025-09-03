package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.application.command.OnboardingCommand;
import com.dh.ondot.member.core.exception.AlreadyOnboardedException;
import com.dh.ondot.member.core.exception.NotFoundMemberException;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public Member getMemberIfExists(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundMemberException(memberId));
    }

    public Member getAndValidateAlreadyOnboarded(Long memberId) {
        Member member = getMemberIfExists(memberId);
        if (!member.isNewMember()) {
            throw new AlreadyOnboardedException(member.getId());
        }
        return member;
    }

    public Long getTotalMemberCount() {
        return memberRepository.count();
    }

    @Transactional
    public Member findOrRegisterOauthMember(UserInfo userInfo, OauthProvider oauthProvider) {
        return memberRepository.findByOauthInfo(userInfo.oauthProviderId(), oauthProvider)
                .orElseGet(() -> registerMemberWithOauth(userInfo, oauthProvider));
    }

    private Member registerMemberWithOauth(UserInfo userInfo, OauthProvider oauthProvider) {
        Member newMember = Member.registerWithOauth(
                userInfo.email(),
                oauthProvider,
                userInfo.oauthProviderId()
        );
        return memberRepository.save(newMember);
    }

    @Transactional
    public Member updatePreparationTime(Long memberId, Integer preparationTime) {
        Member member = getMemberIfExists(memberId);
        member.updatePreparationTime(preparationTime);
        return member;
    }

    @Transactional
    public Member updateOnboardingInfo(Member member, OnboardingCommand command) {
        member.updateOnboarding(
                command.preparationTime(), command.alarmMode(),
                command.isSnoozeEnabled(), command.snoozeInterval(), command.snoozeCount(),
                command.soundCategory(), command.ringTone(), command.volume()
        );
        return member;
    }

    @Transactional
    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
