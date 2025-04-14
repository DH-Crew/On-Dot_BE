package com.dh.ondot.member.app;

import com.dh.ondot.member.app.dto.Token;
import com.dh.ondot.member.domain.*;
import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthFacade {
    private final TokenFacade tokenFacade;
    private final OauthApiFactory oauthApiFactory;
    private final MemberRepository memberRepository;

    @Transactional
    public Token loginWithOAuth(OauthProvider oauthProvider, String accessToken) {
        OauthApi oauthApi = oauthApiFactory.getOauthApi(oauthProvider);
        UserInfo userInfo = oauthApi.fetchUser(accessToken);

        Member member = memberRepository.findByOauthInfo(OauthInfo.of(oauthProvider, userInfo.oauthProviderId()))
                .orElseGet(() -> registerMemberWithOauth(userInfo, oauthProvider));

        return tokenFacade.issue(member.getId());
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
