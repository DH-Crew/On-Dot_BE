package com.dh.ondot.member.application;

import com.dh.ondot.member.api.response.LoginResponse;
import com.dh.ondot.member.application.dto.Token;
import com.dh.ondot.member.domain.*;
import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.service.MemberService;
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
    private final MemberService memberService;

    @Transactional
    public LoginResponse loginWithOAuth(OauthProvider oauthProvider, String accessToken) {
        OauthApi oauthApi = oauthApiFactory.getOauthApi(oauthProvider);
        UserInfo userInfo = oauthApi.fetchUser(accessToken);

        Member member = memberService.findOrRegisterOauthMember(userInfo, oauthProvider);

        boolean isNewMember = member.isNewMember();
        Token token = tokenFacade.issue(member.getId());
        if (isNewMember) {
            return LoginResponse.of(member.getId(),token.accessToken(), "", true);
        } else {
            return LoginResponse.of(member.getId(), token.accessToken(), token.refreshToken(), false);
        }
    }
}
