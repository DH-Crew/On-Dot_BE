package com.dh.ondot.member.infra.oauth

import com.dh.ondot.member.core.AppleProperties
import com.dh.ondot.member.core.exception.AppleAuthorizationCodeExpiredException
import com.dh.ondot.member.core.exception.OauthUserFetchFailedException
import com.dh.ondot.member.domain.OauthApi
import com.dh.ondot.member.domain.dto.UserInfo
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.infra.dto.ApplePublicKeyResponse
import com.dh.ondot.member.infra.dto.AppleSocialTokenInfoResponseDto
import com.dh.ondot.member.infra.jwt.AppleJwtUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

@Component
class AppleOauthApi(
    private val appleProperties: AppleProperties,
    private val appleJwtUtil: AppleJwtUtil,
) : OauthApi {

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient: RestClient = RestClient.create()

    /**
     * @param authorizationCode - 애플에서 발급된 authorizationCode
     * @return UserInfo( sub -> providerId, email )
     */
    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500),
    )
    override fun fetchUser(accessToken: String): UserInfo {
        try {
            val tokenDto = requestTokenToApple(accessToken)

            val appleKeys = restClient.get()
                .uri(appleProperties.audience + "/auth/keys")
                .retrieve()
                .body(ApplePublicKeyResponse::class.java)

            return appleJwtUtil.parseIdToken(appleKeys!!, tokenDto.idToken)
        } catch (e: Exception) {
            log.error("Apple OAuth 사용자 정보 조회 실패: {}", e.message, e)
            throw OauthUserFetchFailedException(OauthProvider.APPLE.name)
        }
    }

    private fun requestTokenToApple(authorizationCode: String): AppleSocialTokenInfoResponseDto {
        try {
            val body = "client_id=${appleProperties.clientId}" +
                "&client_secret=${appleJwtUtil.generateClientSecret()}" +
                "&code=$authorizationCode" +
                "&grant_type=${appleProperties.grantType}"

            val response = restClient.post()
                .uri(appleProperties.audience + "/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("User-Agent", "Ondot-Server/1.0")
                .body(body)
                .retrieve()
                .toEntity(AppleSocialTokenInfoResponseDto::class.java)

            return response.body!!
        } catch (e: HttpClientErrorException) {
            log.error("Apple 토큰 요청 실패 - status: {}, body: {}", e.statusCode, e.responseBodyAsString)
            if (e.statusCode == HttpStatus.BAD_REQUEST && e.responseBodyAsString.contains("invalid_grant")) {
                throw AppleAuthorizationCodeExpiredException()
            }
            throw OauthUserFetchFailedException(OauthProvider.APPLE.name)
        } catch (e: Exception) {
            log.error("Apple 토큰 요청 중 예외 발생: {}", e.message, e)
            throw OauthUserFetchFailedException(OauthProvider.APPLE.name)
        }
    }
}
