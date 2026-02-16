package com.dh.ondot.member.infra.oauth

import com.dh.ondot.member.core.exception.OauthUserFetchFailedException
import com.dh.ondot.member.domain.OauthApi
import com.dh.ondot.member.domain.dto.UserInfo
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.infra.dto.KakaoUserInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

@Component
class KakaoOauthApi : OauthApi {

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient: RestClient = RestClient.create()

    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500),
    )
    override fun fetchUser(accessToken: String): UserInfo {
        try {
            val response = restClient.get()
                .uri(USERINFO_URL)
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .body(KakaoUserInfoResponse::class.java)

            val oauthProviderId = response!!.id.toString()
            val email = response.kakaoAccount.email

            return UserInfo(oauthProviderId, email)
        } catch (ex: RestClientResponseException) {
            log.error(
                "Kakao API error: status={} responseBody={}",
                ex.statusCode,
                ex.responseBodyAsString,
                ex,
            )
            throw OauthUserFetchFailedException(OauthProvider.KAKAO.name)
        } catch (ex: RestClientException) {
            log.error("Failed to call Kakao userinfo endpoint", ex)
            throw OauthUserFetchFailedException(OauthProvider.KAKAO.name)
        }
    }

    companion object {
        private const val USERINFO_URL = "https://kapi.kakao.com/v2/user/me"
    }
}
