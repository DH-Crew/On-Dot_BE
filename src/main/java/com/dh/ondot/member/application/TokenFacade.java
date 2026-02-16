package com.dh.ondot.member.application;

import com.dh.ondot.member.application.dto.Token;
import com.dh.ondot.member.application.dto.TokenInfo;
import com.dh.ondot.member.core.JwtProperties;
import com.dh.ondot.member.core.exception.RefreshTokenExpiredException;
import com.dh.ondot.member.core.exception.TokenBlacklistedException;
import com.dh.ondot.member.infra.RedisTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenFacade {
    public static final long HOURS_IN_MILLIS = 60 * 60 * 1000L;

    private final JwtProperties jwtProperties;
    private final TokenManager tokenManager;
    private final RedisTokenRepository redisTokenRepository;
    private long accessTokenTime;
    private long refreshTokenTime;

    @PostConstruct
    public void init() {
        this.accessTokenTime = jwtProperties.getAccessTokenExpireTimeInHours() * HOURS_IN_MILLIS;
        this.refreshTokenTime = jwtProperties.getRefreshTokenExpireTimeInHours() * HOURS_IN_MILLIS;
    }

    public Token issue(Long memberId) {
        String newAccessToken = tokenManager.createToken(memberId, accessTokenTime);
        String newRefreshToken = tokenManager.createToken(memberId, refreshTokenTime);

        return new Token(newAccessToken, newRefreshToken);
    }

    public Token reissue(String oldRefreshToken) {
        TokenInfo tokenInfo = tokenManager.parseClaimsFromRefreshToken(oldRefreshToken);
        String jti = tokenInfo.tokenId();
        String memberId = tokenInfo.memberId();

        if (redisTokenRepository.isBlacklisted(jti)) {
            throw new TokenBlacklistedException();
        }

        redisTokenRepository.addBlacklistToken(jti, getRemainingDuration(tokenInfo.expiration()));

        return issue(Long.valueOf(memberId));
    }

    public void logout(String refreshToken) {
        try {
            TokenInfo tokenInfo = tokenManager.parseClaimsFromRefreshToken(refreshToken);
            String jti = tokenInfo.tokenId();
            Instant expiration = tokenInfo.expiration();

            redisTokenRepository.addBlacklistToken(jti, getRemainingDuration(expiration));
        } catch (Exception e) {
            log.warn("Invalid refresh token during logout. Token: {}", refreshToken, e);
        }
    }

    public Long validateToken(String accessToken) {
        TokenInfo tokenInfo = tokenManager.parseClaims(accessToken);
        String memberId = tokenInfo.memberId();

        return Long.parseLong(memberId);
    }

    private Duration getRemainingDuration(Instant expiration) {
        Instant now = Instant.now();
        if(now.isAfter(expiration)) {
            throw new RefreshTokenExpiredException();
        }

        return Duration.between(now, expiration);
    }
}
