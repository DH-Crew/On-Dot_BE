package com.dh.ondot.member.application

import com.dh.ondot.member.application.dto.Token
import com.dh.ondot.member.application.dto.TokenInfo
import com.dh.ondot.member.core.JwtProperties
import com.dh.ondot.member.core.TokenExtractor
import com.dh.ondot.member.core.exception.InvalidTokenHeaderException
import com.dh.ondot.member.core.exception.RefreshTokenExpiredException
import com.dh.ondot.member.core.exception.TokenBlacklistedException
import com.dh.ondot.member.core.exception.TokenMissingException
import com.dh.ondot.member.infra.RedisTokenRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class TokenFacade(
    private val jwtProperties: JwtProperties,
    private val tokenManager: TokenManager,
    private val redisTokenRepository: RedisTokenRepository,
) {
    private val log = LoggerFactory.getLogger(TokenFacade::class.java)
    private var accessTokenTime: Long = 0
    private var refreshTokenTime: Long = 0

    @PostConstruct
    fun init() {
        accessTokenTime = jwtProperties.accessTokenExpireTimeInHours * HOURS_IN_MILLIS
        refreshTokenTime = jwtProperties.refreshTokenExpireTimeInHours * HOURS_IN_MILLIS
    }

    fun issue(memberId: Long): Token {
        val newAccessToken = tokenManager.createToken(memberId, accessTokenTime)
        val newRefreshToken = tokenManager.createToken(memberId, refreshTokenTime)

        return Token(newAccessToken, newRefreshToken)
    }

    fun reissue(oldRefreshToken: String): Token {
        val tokenInfo: TokenInfo = tokenManager.parseClaimsFromRefreshToken(oldRefreshToken)
        val jti = tokenInfo.tokenId
        val memberId = tokenInfo.memberId

        if (redisTokenRepository.isBlacklisted(jti)) {
            throw TokenBlacklistedException()
        }

        redisTokenRepository.addBlacklistToken(jti, getRemainingDuration(tokenInfo.expiration))

        return issue(memberId.toLong())
    }

    fun logout(refreshToken: String) {
        try {
            val tokenInfo: TokenInfo = tokenManager.parseClaimsFromRefreshToken(refreshToken)
            val jti = tokenInfo.tokenId
            val expiration = tokenInfo.expiration

            redisTokenRepository.addBlacklistToken(jti, getRemainingDuration(expiration))
        } catch (e: Exception) {
            log.warn("Invalid refresh token during logout. Token: {}", refreshToken, e)
        }
    }

    fun logoutByHeader(authorizationHeader: String) {
        try {
            val refreshToken = TokenExtractor.extract(authorizationHeader)
            logout(refreshToken)
        } catch (_: TokenMissingException) {
        } catch (_: InvalidTokenHeaderException) {
        }
    }

    fun validateToken(accessToken: String): Long {
        val tokenInfo: TokenInfo = tokenManager.parseClaims(accessToken)
        val memberId = tokenInfo.memberId

        return memberId.toLong()
    }

    private fun getRemainingDuration(expiration: Instant): Duration {
        val now = Instant.now()
        if (now.isAfter(expiration)) {
            throw RefreshTokenExpiredException()
        }

        return Duration.between(now, expiration)
    }

    companion object {
        const val HOURS_IN_MILLIS: Long = 60 * 60 * 1000L
    }
}
