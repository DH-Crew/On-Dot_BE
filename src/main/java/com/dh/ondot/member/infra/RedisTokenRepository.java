package com.dh.ondot.member.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String memberId, String refreshToken, long refreshTokenTime) {
        redisTemplate.opsForValue().set(memberId, refreshToken, refreshTokenTime, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String memberId) {
        return redisTemplate.opsForValue().get(memberId);
    }

    public boolean isBlacklisted(String jti) {
        return redisTemplate.opsForValue().get("blacklist:" + jti) != null;
    }

    public void addBlacklistToken(String jti, Duration expiration) {
        redisTemplate.opsForValue().set("blacklist:" + jti, jti, expiration);
    }
}