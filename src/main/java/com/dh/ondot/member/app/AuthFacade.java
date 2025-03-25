package com.dh.ondot.member.app;

import com.dh.ondot.member.api.response.AccessToken;
import com.dh.ondot.member.api.response.Token;
import com.dh.ondot.member.api.response.UserInfo;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthFacade {
    private final OauthApi oauthApi;
    private final TokenFacade tokenFacade;
    private final MemberRepository memberRepository;

    @Transactional
    public Token kakaoLogin(String code) {
        String accessToken = oauthApi.getAccessToken(code);
        UserInfo userInfo = oauthApi.getOauthUser(accessToken);

        Member member = memberRepository.findByOauthProviderId(userInfo.oauthProviderId())
                .orElseGet(() -> creatMember(userInfo, "kakao"));

        return tokenFacade.issue(member.getId());
    }

    public Token reissueToken(String oldRefreshToken) {
        Token token = tokenFacade.reissue(oldRefreshToken);

        return token;
    }

    public AccessToken issueTokenForTest() {
        Token token = tokenFacade.issue(1L);

        return new AccessToken(token.accessToken());
    }

    private Member creatMember(UserInfo userInfo, String oauthProvider) {
        Member newMember = Member.create(
                userInfo.email(),
                oauthProvider,
                userInfo.oauthProviderId()
        );
        return memberRepository.save(newMember);
    }
}