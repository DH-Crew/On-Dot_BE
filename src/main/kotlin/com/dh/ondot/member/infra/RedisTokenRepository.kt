package com.dh.ondot.member.infra

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisTokenRepository(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun isBlacklisted(jti: String): Boolean {
        return redisTemplate.opsForValue().get(toBlacklistKey(jti)) != null
    }

    fun addBlacklistToken(jti: String, expiration: Duration) {
        redisTemplate.opsForValue().set(toBlacklistKey(jti), jti, expiration)
    }

    private fun toBlacklistKey(jti: String): String {
        return BLACKLIST_KEY_PREFIX + jti
    }

    companion object {
        private const val BLACKLIST_KEY_PREFIX = "blacklist:"
    }
}
