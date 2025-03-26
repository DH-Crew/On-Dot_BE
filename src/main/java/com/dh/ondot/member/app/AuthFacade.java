package com.dh.ondot.member.app;

import com.dh.ondot.member.app.dto.Token;
import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.OauthApi;
import com.dh.ondot.member.domain.OauthInfo;
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
    public Token loginWithOAuth(String oauthProvider, String accessToken) {
        UserInfo userInfo = oauthApi.fetchUser(accessToken);

        Member member = memberRepository.findByOauthInfo(OauthInfo.of(oauthProvider, userInfo.oauthProviderId()))
                .orElseGet(() -> creatMember(userInfo, oauthProvider));

        return tokenFacade.issue(member.getId());
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
