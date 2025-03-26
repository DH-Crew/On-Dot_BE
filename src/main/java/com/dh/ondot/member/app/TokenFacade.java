package com.dh.ondot.member.app;

import com.dh.ondot.member.app.dto.Token;
import com.dh.ondot.member.app.dto.TokenInfo;
import com.dh.ondot.member.core.JwtProperties;
import com.dh.ondot.member.core.exception.TokenBlacklistedException;
import com.dh.ondot.member.infra.RedisTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

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
        this.accessTokenTime = jwtProperties.accessTokenExpireTimeInHours() * HOURS_IN_MILLIS;
        this.refreshTokenTime = jwtProperties.refreshTokenExpireTimeInHours() * HOURS_IN_MILLIS;
    }

    public Token issue(Long memberId) {
        String newAccessToken = tokenManager.createToken(memberId, accessTokenTime);
        String newRefreshToken = tokenManager.createToken(memberId, refreshTokenTime);

        redisTokenRepository.saveRefreshToken(memberId.toString(), newRefreshToken, refreshTokenTime);

        return new Token(newAccessToken, newRefreshToken);
    }

    public Token reissue(String oldRefreshToken) {
        TokenInfo tokenInfo = tokenManager.parseClaimsFromRefreshToken(oldRefreshToken);
        String jti = tokenInfo.tokenId();
        String memberId = tokenInfo.memberId();

        if (redisTokenRepository.isBlacklisted(jti)) {
            String currentRefreshToken = redisTokenRepository.getRefreshToken(memberId);
            Date expiration = tokenManager.parseClaimsFromRefreshToken(currentRefreshToken).expiration();
            redisTokenRepository.addBlacklistToken(currentRefreshToken, getRemainingDuration(expiration));

            throw new TokenBlacklistedException();
        }

        redisTokenRepository.addBlacklistToken(jti, getRemainingDuration(tokenInfo.expiration()));

        return issue(Long.valueOf(memberId));
    }

    public Long validateToken(String accessToken) {
        TokenInfo tokenInfo = tokenManager.parseClaims(accessToken);
        String memberId = tokenInfo.memberId();

        return Long.parseLong(memberId);
    }

    private Duration getRemainingDuration(Date expiration) {
        Instant now = Instant.now();
        Instant expirationTime = expiration.toInstant();

        return Duration.between(now, expirationTime);
    }
}