package com.dh.ondot.schedule.infra.redis;

import com.dh.ondot.schedule.domain.PlaceHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PlaceHistoryRedisRepository {
    private static final String KEY_PREFIX  = "place:history:";
    private static final int MAX_HISTORY = 10;
    private static final Duration TTL_PER_ITEM = Duration.ofDays(30);

    private final RedisTemplate<String, String> redisTemplate;
    private final PlaceHistoryJsonConverter converter;

    public void push(PlaceHistory history) {
        String key = key(history.memberId());
        String value = converter.toJson(history);
        double score = history.searchedAt().getEpochSecond();

        redisTemplate.opsForZSet().add(key, value, score);

        // 최근 기록 10개만 남기고 삭제
        long size = safeLong(redisTemplate.opsForZSet().size(key));
        long excess = size - MAX_HISTORY;
        if (excess > 0) {
            redisTemplate.opsForZSet().removeRange(key, 0, excess - 1);
        }
    }

    public List<PlaceHistory> findRecent(Long memberId) {
        String key = key(memberId);

        Set<String> jsonSet = redisTemplate.opsForZSet()
                .reverseRange(key, 0, MAX_HISTORY - 1);
        if (jsonSet == null || jsonSet.isEmpty()) {
            return Collections.emptyList();
        }

        return jsonSet.stream()
                .map(converter::fromJson)
                .toList();
    }

    public long deleteExpired(String key) {
        long cutoff = Instant.now().minus(TTL_PER_ITEM).getEpochSecond();
        return safeLong(redisTemplate.opsForZSet()
                .removeRangeByScore(key, 0, cutoff));
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }
}
