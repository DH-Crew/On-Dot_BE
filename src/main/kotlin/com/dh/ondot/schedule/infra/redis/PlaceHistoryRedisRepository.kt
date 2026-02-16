package com.dh.ondot.schedule.infra.redis

import com.dh.ondot.schedule.domain.PlaceHistory
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.Instant

@Repository
class PlaceHistoryRedisRepository(
    private val redisTemplate: RedisTemplate<String, String>,
    private val converter: PlaceHistoryJsonConverter,
) {
    fun push(history: PlaceHistory) {
        val key = key(history.memberId)

        // 중복 체크
        val existing = redisTemplate.opsForZSet().range(key, 0, -1)
        val duplicateJson = findDuplicate(existing, history)

        val value = converter.toJson(history)
        val score = history.searchedAt.epochSecond.toDouble()

        redisTemplate.execute(object : SessionCallback<List<Any>?> {
            @Suppress("UNCHECKED_CAST")
            override fun <K : Any, V : Any> execute(operations: RedisOperations<K, V>): List<Any>? {
                operations.multi()

                if (duplicateJson != null) {
                    (operations as RedisOperations<String, String>).opsForZSet().remove(key, duplicateJson)
                }

                (operations as RedisOperations<String, String>).opsForZSet().add(key, value, score)

                // 10개만 남기도록
                (operations as RedisOperations<String, String>).opsForZSet().removeRange(key, 0, (-MAX_HISTORY - 1).toLong())
                return operations.exec()
            }
        })
    }

    fun findRecent(memberId: Long): List<PlaceHistory> {
        val key = key(memberId)

        val jsonSet = redisTemplate.opsForZSet()
            .reverseRange(key, 0, (MAX_HISTORY - 1).toLong())
        if (jsonSet.isNullOrEmpty()) {
            return emptyList()
        }

        return jsonSet.stream()
            .map { converter.fromJson(it) }
            .toList()
    }

    fun deleteExpired(key: String): Long {
        val cutoff = Instant.now().minus(TTL_PER_ITEM).epochSecond.toDouble()
        return safeLong(redisTemplate.opsForZSet()
            .removeRangeByScore(key, 0.0, cutoff))
    }

    fun removeByTimestamp(memberId: Long, searchedAt: Instant): Long {
        val key = key(memberId)
        val score = searchedAt.epochSecond.toDouble()
        return safeLong(redisTemplate.opsForZSet()
            .removeRangeByScore(key, score, score))
    }

    private fun findDuplicate(existingJsons: Set<String>?, newHistory: PlaceHistory): String? {
        if (existingJsons.isNullOrEmpty()) {
            return null
        }

        for (json in existingJsons) {
            val existing = converter.fromJson(json)
            if (isDuplicate(existing, newHistory)) {
                return json
            }
        }
        return null
    }

    private fun isDuplicate(a: PlaceHistory, b: PlaceHistory): Boolean {
        return a.title == b.title
            && a.longitude.compareTo(b.longitude) == 0
            && a.latitude.compareTo(b.latitude) == 0
    }

    private fun key(memberId: Long): String {
        return KEY_PREFIX + memberId
    }

    private fun safeLong(value: Long?): Long {
        return value ?: 0L
    }

    companion object {
        private const val KEY_PREFIX = "place:history:"
        private const val MAX_HISTORY = 10
        private val TTL_PER_ITEM: Duration = Duration.ofDays(30)
    }
}
