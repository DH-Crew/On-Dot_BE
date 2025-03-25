package com.dh.ondot.member.infra;

import com.dh.ondot.member.api.response.UserInfo;
import com.dh.ondot.member.app.OauthApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoOauthApi implements OauthApi {
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Value("${oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private final RestClient restClient = RestClient.create();

    @Override
    public String getAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret);

        Map response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(Map.class);

        return (String) response.get("access_token");
    }

    @Override
    public UserInfo getOauthUser(String accessToken) {
        Map response = restClient.get()
                .uri(USERINFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String oauthProviderId = String.valueOf(response.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        return new UserInfo(oauthProviderId, email);
    }
}