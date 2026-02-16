package com.dh.ondot.member.infra.oauth

import com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SOCIAL_LOGIN
import com.dh.ondot.core.exception.UnsupportedException
import com.dh.ondot.member.domain.OauthApi
import com.dh.ondot.member.domain.OauthApiFactory
import com.dh.ondot.member.domain.enums.OauthProvider
import org.springframework.stereotype.Component

@Component
class OauthApiFactoryImpl(
    oauthApis: List<OauthApi>,
) : OauthApiFactory {

    private val oauthApiMap: Map<OauthProvider, OauthApi> = mapOf(
        OauthProvider.APPLE to oauthApis.first { it is AppleOauthApi },
        OauthProvider.KAKAO to oauthApis.first { it is KakaoOauthApi },
    )

    override fun getOauthApi(provider: OauthProvider): OauthApi {
        return oauthApiMap[provider]
            ?: throw UnsupportedException(UNSUPPORTED_SOCIAL_LOGIN, provider.name)
    }
}
