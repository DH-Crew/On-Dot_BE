package com.dh.ondot.schedule.infra.redis

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class PlaceHistoryCleaner(
    private val redisTemplate: RedisTemplate<String, String>,
    private val repository: PlaceHistoryRedisRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 매 시 정각에 최근 검색 기록 ZSET의 만료 데이터를 정리한다.
     * SCAN 으로 키를 스트리밍하며, 샘플링 → 만료 삭제 → 비율 계산 로직을 반복한다.
     */
    @Scheduled(cron = "0 0 * * * *")
    fun cleanExpiredHistories() {
        var iteration = 0
        while (iteration < MAX_ITERATIONS) {
            val sampleKeys = scanSampleKeys(SAMPLE_SIZE)
            if (sampleKeys.isEmpty()) {
                log.debug("PlaceHistoryCleaner \u2011 no keys found, exit.")
                return
            }

            var cleaned = 0
            for (key in sampleKeys) {
                val removed = repository.deleteExpired(key)
                if (removed > 0) {
                    cleaned++
                    log.debug("PlaceHistoryCleaner \u2011 key={} expiredRemoved={}", key, removed)
                }
            }

            val ratio = cleaned.toDouble() / sampleKeys.size
            if (ratio < THRESHOLD) {
                break
            }
            iteration++
        }
    }

    /** SCAN 명령으로 PREFIX 에 매칭되는 키 중 최대 n개만 수집 */
    private fun scanSampleKeys(limit: Int): List<String> {
        val keys = ArrayList<String>(limit)
        val options = ScanOptions.scanOptions()
            .match("$KEY_PREFIX*")
            .count(20)
            .build()

        try {
            redisTemplate.connectionFactory!!
                .connection
                .scan(options).use { cursor ->
                    while (cursor.hasNext() && keys.size < limit) {
                        val key = String(cursor.next(), StandardCharsets.UTF_8)
                        keys.add(key)
                    }
                }
        } catch (e: Exception) {
            log.warn("PlaceHistoryCleaner \u2011 SCAN failed : {}", e.message, e)
        }
        return keys
    }

    companion object {
        private const val KEY_PREFIX = "place:history:"
        private const val SAMPLE_SIZE = 5
        private const val THRESHOLD = 0.4
        private const val MAX_ITERATIONS = 5
    }
}
