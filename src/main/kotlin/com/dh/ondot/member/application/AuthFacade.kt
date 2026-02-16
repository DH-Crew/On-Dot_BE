package com.dh.ondot.member.application

import com.dh.ondot.member.presentation.response.LoginResponse
import com.dh.ondot.member.application.dto.Token
import com.dh.ondot.member.domain.OauthApiFactory
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.domain.service.MemberService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthFacade(
    private val tokenFacade: TokenFacade,
    private val oauthApiFactory: OauthApiFactory,
    private val memberService: MemberService,
) {
    private val log = LoggerFactory.getLogger(AuthFacade::class.java)

    @Transactional
    fun loginWithOAuth(oauthProvider: OauthProvider, accessToken: String): LoginResponse {
        val oauthApi = oauthApiFactory.getOauthApi(oauthProvider)
        val userInfo = oauthApi.fetchUser(accessToken)

        val member = memberService.findOrRegisterOauthMember(userInfo, oauthProvider)

        val isNewMember = member.isNewMember()
        val token: Token = tokenFacade.issue(member.id)
        return if (isNewMember) {
            LoginResponse.of(member.id, token.accessToken, "", true)
        } else {
            LoginResponse.of(member.id, token.accessToken, token.refreshToken, false)
        }
    }
}
