package com.dh.ondot.member.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisTokenRepository {

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlacklisted(String jti) {
        return redisTemplate.opsForValue().get(toBlacklistKey(jti)) != null;
    }

    public void addBlacklistToken(String jti, Duration expiration) {
        redisTemplate.opsForValue().set(toBlacklistKey(jti), jti, expiration);
    }

    private String toBlacklistKey(String jti) {
        return BLACKLIST_KEY_PREFIX + jti;
    }
}
