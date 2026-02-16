package com.dh.ondot.member.presentation

import com.dh.ondot.member.presentation.response.AccessToken
import com.dh.ondot.member.presentation.response.LoginResponse
import com.dh.ondot.member.presentation.swagger.AuthSwagger
import com.dh.ondot.member.application.AuthFacade
import com.dh.ondot.member.application.TokenFacade
import com.dh.ondot.member.application.dto.Token
import com.dh.ondot.member.core.TokenExtractor
import com.dh.ondot.member.domain.enums.OauthProvider
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authFacade: AuthFacade,
    private val tokenFacade: TokenFacade,
) : AuthSwagger {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login/oauth")
    override fun loginWithOAuth(
        @RequestParam("provider") provider: OauthProvider,
        @RequestParam("access_token") accessToken: String,
    ): LoginResponse {
        val result = authFacade.loginWithOAuth(provider, accessToken)
        return LoginResponse.from(result)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/reissue")
    override fun reissue(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
    ): Token {
        val refreshToken = TokenExtractor.extract(token)
        return tokenFacade.reissue(refreshToken)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    override fun logout(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
    ) {
        tokenFacade.logoutByHeader(token)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/test/token")
    override fun testToken(): AccessToken {
        val token = tokenFacade.issue(1L)
        return AccessToken(token.accessToken)
    }
}
