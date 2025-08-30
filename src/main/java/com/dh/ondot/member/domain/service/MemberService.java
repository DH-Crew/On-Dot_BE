package com.dh.ondot.member.domain.service;

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

    @Transactional
    public Member updatePreparationTime(Long memberId, Integer preparationTime) {
        Member member = getMemberIfExists(memberId);
        member.updatePreparationTime(preparationTime);
        return member;
    }

    @Transactional
    public Member findOrRegisterOauthMember(UserInfo userInfo, OauthProvider oauthProvider) {
        return memberRepository.findByOauthInfo(oauthProvider, userInfo.oauthProviderId())
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
}
