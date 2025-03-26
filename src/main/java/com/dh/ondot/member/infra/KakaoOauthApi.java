package com.dh.ondot.member.infra;

import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.domain.OauthApi;
import com.dh.ondot.member.infra.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoOauthApi implements OauthApi {
    private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient = RestClient.create();

    @Override
    public UserInfo fetchUser(String accessToken) {
        KakaoUserInfoResponse response = restClient.get()
                .uri(USERINFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponse.class);

        String oauthProviderId = String.valueOf(response.id());
        String email = response.kakao_account().email();

        return new UserInfo(oauthProviderId, email);
    }
}