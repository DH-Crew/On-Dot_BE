package com.dh.ondot.schedule.infra.redis;

import com.dh.ondot.schedule.domain.PlaceHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
        String key = key(history.getMemberId());

        // 중복 체크
        Set<String> existing = redisTemplate.opsForZSet().range(key, 0, -1);
        String duplicateJson = findDuplicate(existing, history);

        String value = converter.toJson(history);
        double score = history.getSearchedAt().getEpochSecond();

        redisTemplate.execute(new SessionCallback<Void>() {
            @SuppressWarnings("unchecked")
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                if (duplicateJson != null) {
                    operations.opsForZSet().remove(key, duplicateJson);
                }

                operations.opsForZSet().add(key, value, score);

                // 10개만 남기도록
                operations.opsForZSet().removeRange(key, 0, -MAX_HISTORY - 1);
                operations.exec();
                return null;
            }
        });
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

    public long removeByTimestamp(Long memberId, Instant searchedAt) {
        String key = key(memberId);
        double score = searchedAt.getEpochSecond();
        return safeLong(redisTemplate.opsForZSet()
                .removeRangeByScore(key, score, score));
    }

    private String findDuplicate(Set<String> existingJsons, PlaceHistory newHistory) {
        if (existingJsons == null || existingJsons.isEmpty()) {
            return null;
        }

        for (String json : existingJsons) {
            PlaceHistory existing = converter.fromJson(json);
            if (isDuplicate(existing, newHistory)) {
                return json;
            }
        }
        return null;
    }

    private boolean isDuplicate(PlaceHistory a, PlaceHistory b) {
        return a.getTitle().equals(b.getTitle())
                && Double.compare(a.getLongitude(), b.getLongitude()) == 0
                && Double.compare(a.getLatitude(), b.getLatitude()) == 0;
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }
}
