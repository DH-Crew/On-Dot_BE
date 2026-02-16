package com.dh.ondot.core

import com.dh.ondot.member.application.TokenFacade
import com.dh.ondot.member.core.exception.InvalidTokenHeaderException
import com.dh.ondot.member.core.exception.TokenMissingException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class TokenInterceptor(
    private val tokenFacade: TokenFacade,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            val jwtToken = request.getHeader(AUTHORIZATION_HEADER)
                ?.takeIf { it.isNotBlank() }
                ?: throw TokenMissingException()

            val accessToken = if (jwtToken.startsWith(BEARER_PREFIX)) {
                jwtToken.substring(BEARER_PREFIX.length)
            } else {
                throw InvalidTokenHeaderException()
            }

            val memberId = tokenFacade.validateToken(accessToken)
            request.setAttribute("memberId", memberId)
        }
        return true
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
