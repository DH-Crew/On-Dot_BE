package com.dh.ondot.member.infra.oauth;

import com.dh.ondot.member.core.exception.UnsupportedSocialLoginException;
import com.dh.ondot.member.domain.OauthApi;
import com.dh.ondot.member.domain.OauthApiFactory;
import com.dh.ondot.member.domain.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OauthApiFactoryImpl implements OauthApiFactory {
    private final Map<OauthProvider, OauthApi> oauthApiMap;

    @Autowired
    public OauthApiFactoryImpl(List<OauthApi> oauthApis) {
        oauthApiMap = Map.of(
                OauthProvider.APPLE, oauthApis.stream().filter(api -> api instanceof AppleOauthApi).findFirst().orElseThrow(),
                OauthProvider.KAKAO, oauthApis.stream().filter(api -> api instanceof KakaoOauthApi).findFirst().orElseThrow()
        );
    }

    @Override
    public OauthApi getOauthApi(OauthProvider provider) {
        return Optional.ofNullable(oauthApiMap.get(provider))
                .orElseThrow(() -> new UnsupportedSocialLoginException(provider.name()));
    }
}
