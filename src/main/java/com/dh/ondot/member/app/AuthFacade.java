package com.dh.ondot.member.app;

import com.dh.ondot.member.api.response.LoginResponse;
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
    public LoginResponse loginWithOAuth(OauthProvider oauthProvider, String accessToken) {
        OauthApi oauthApi = oauthApiFactory.getOauthApi(oauthProvider);
        UserInfo userInfo = oauthApi.fetchUser(accessToken);

        Member member = memberRepository.findByEmail(userInfo.email())
                .orElseGet(() -> registerMemberWithOauth(userInfo, oauthProvider));

        boolean isNewMember = member.isNewMember();
        if (isNewMember) {
            return LoginResponse.of("", "", true);
        } else {
            Token token = tokenFacade.issue(member.getId());
            return LoginResponse.of(token.accessToken(), token.refreshToken(), false);
        }
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
