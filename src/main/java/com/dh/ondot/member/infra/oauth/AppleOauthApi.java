package com.dh.ondot.member.infra.oauth;

import com.dh.ondot.member.core.AppleProperties;
import com.dh.ondot.member.core.exception.AppleAuthorizationCodeExpiredException;
import com.dh.ondot.member.core.exception.OauthUserFetchFailedException;
import com.dh.ondot.member.domain.OauthApi;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.infra.dto.ApplePublicKeyResponse;
import com.dh.ondot.member.infra.dto.AppleSocialTokenInfoResponseDto;
import com.dh.ondot.member.infra.jwt.AppleJwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOauthApi implements OauthApi {
    private final AppleProperties appleProperties;
    private final RestClient restClient = RestClient.create();
    private final AppleJwtUtil appleJwtUtil;

    /**
     * @param authorizationCode - 애플에서 발급된 authorizationCode
     * @return UserInfo( sub -> providerId, email )
     */
    @Override
    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public UserInfo fetchUser(String authorizationCode) {
        try {
            AppleSocialTokenInfoResponseDto tokenDto = requestTokenToApple(authorizationCode);

            ApplePublicKeyResponse appleKeys = restClient.get()
                    .uri(appleProperties.audience() + "/auth/keys")
                    .retrieve()
                    .body(ApplePublicKeyResponse.class);

            return appleJwtUtil.parseIdToken(appleKeys, tokenDto.idToken());
        } catch (Exception e) {
            log.error("Apple OAuth 사용자 정보 조회 실패: {}", e.getMessage(), e);
            throw new OauthUserFetchFailedException(OauthProvider.APPLE.name());
        }
    }

    private AppleSocialTokenInfoResponseDto requestTokenToApple(String authorizationCode) {
        try {
            String body = "client_id=" + appleProperties.clientId()
                    + "&client_secret=" + appleJwtUtil.generateClientSecret()
                    + "&code=" + authorizationCode
                    + "&grant_type=" + appleProperties.grantType();

            ResponseEntity<AppleSocialTokenInfoResponseDto> response = restClient.post()
                    .uri(appleProperties.audience() + "/auth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(AppleSocialTokenInfoResponseDto.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Apple 토큰 요청 실패 - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains("invalid_grant")) {
                throw new AppleAuthorizationCodeExpiredException();
            }
            throw new OauthUserFetchFailedException(OauthProvider.APPLE.name());
        } catch (Exception e) {
            log.error("Apple 토큰 요청 중 예외 발생: {}", e.getMessage(), e);
            throw new OauthUserFetchFailedException(OauthProvider.APPLE.name());
        }
    }
}
