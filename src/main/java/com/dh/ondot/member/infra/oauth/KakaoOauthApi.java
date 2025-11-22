package com.dh.ondot.member.infra.oauth;

import com.dh.ondot.member.core.exception.OauthUserFetchFailedException;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.domain.OauthApi;
import com.dh.ondot.member.infra.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOauthApi implements OauthApi {
    private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient = RestClient.create();

    @Override
    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public UserInfo fetchUser(String accessToken) {
        try {
            KakaoUserInfoResponse response = restClient.get()
                    .uri(USERINFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserInfoResponse.class);

            String oauthProviderId = String.valueOf(response.id());
            String email = response.kakao_account().email();

            return new UserInfo(oauthProviderId, email);
        } catch (RestClientResponseException ex) {
            log.error(
                    "Kakao API error: status={} responseBody={}",
                    ex.getRawStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex
            );
            throw new OauthUserFetchFailedException(OauthProvider.KAKAO.name());

        } catch (RestClientException ex) {
            log.error("Failed to call Kakao userinfo endpoint", ex);
            throw new OauthUserFetchFailedException(OauthProvider.KAKAO.name());
        }
    }
}
