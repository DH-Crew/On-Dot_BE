package com.dh.ondot.member.application

import com.dh.ondot.member.application.dto.TokenInfo
import com.dh.ondot.member.core.exception.RefreshTokenExpiredException
import com.dh.ondot.member.core.exception.TokenExpiredException
import com.dh.ondot.member.core.exception.TokenInvalidException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class TokenManager(
    @Value("\${jwt.secret}")
    private val secret: String,
) {
    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun initKey() {
        val keyBytes = Decoders.BASE64.decode(secret)
        secretKey = Keys.hmacShaKeyFor(keyBytes)
    }

    fun createToken(memberId: Long, expirationTime: Long): String {
        val claims = Jwts.claims().subject(memberId.toString()).build()
        val jti = UUID.randomUUID().toString().substring(0, 16) + memberId
        val now = Date()

        return Jwts.builder()
            .id(jti)
            .issuer(TOKEN_ISSUER)
            .claims(claims)
            .issuedAt(now)
            .expiration(Date(now.time + expirationTime))
            .signWith(secretKey)
            .compact()
    }

    fun parseClaims(token: String): TokenInfo {
        try {
            val claims = Jwts.parser()
                .requireIssuer(TOKEN_ISSUER)
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
            return TokenInfo(claims.id, claims.subject, claims.expiration.toInstant())
        } catch (ex: ExpiredJwtException) {
            throw TokenExpiredException()
        } catch (ex: JwtException) {
            throw TokenInvalidException()
        }
    }

    fun parseClaimsFromRefreshToken(token: String): TokenInfo {
        try {
            val claims = Jwts.parser()
                .requireIssuer(TOKEN_ISSUER)
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
            return TokenInfo(claims.id, claims.subject, claims.expiration.toInstant())
        } catch (ex: ExpiredJwtException) {
            throw RefreshTokenExpiredException()
        } catch (ex: JwtException) {
            throw TokenInvalidException()
        }
    }

    companion object {
        const val TOKEN_ISSUER: String = "ON_DOT"
    }
}
