package com.dh.ondot.schedule.infra.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceHistoryCleaner {

    private static final String KEY_PREFIX = "place:history:";
    private static final int SAMPLE_SIZE = 5;
    private static final double THRESHOLD = 0.4;
    private static final int MAX_ITERATIONS = 5;

    private final RedisTemplate<String, String> redisTemplate;
    private final PlaceHistoryRedisRepository repository;

    /**
     * 매 시 정각에 최근 검색 기록 ZSET의 만료 데이터를 정리한다.
     * SCAN 으로 키를 스트리밍하며, 샘플링 → 만료 삭제 → 비율 계산 로직을 반복한다.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiredHistories() {

        int iteration = 0;
        while (iteration < MAX_ITERATIONS) {

            List<String> sampleKeys = scanSampleKeys(SAMPLE_SIZE);
            if (sampleKeys.isEmpty()) {
                log.debug("PlaceHistoryCleaner ‑ no keys found, exit.");
                return;
            }

            int cleaned = 0;
            for (String key : sampleKeys) {
                long removed = repository.deleteExpired(key);
                if (removed > 0) {
                    cleaned++;
                    log.debug("PlaceHistoryCleaner ‑ key={} expiredRemoved={}", key, removed);
                }
            }

            double ratio = (double) cleaned / sampleKeys.size();
            if (ratio < THRESHOLD) {
                break;
            }
            iteration++;
        }
    }

    /**  SCAN 명령으로 PREFIX 에 매칭되는 키 중 최대 n개만 수집  */
    private List<String> scanSampleKeys(int limit) {

        List<String> keys = new ArrayList<>(limit);
        ScanOptions options = ScanOptions.scanOptions()
                .match(KEY_PREFIX + "*")
                .count(20)
                .build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options)) {

            while (cursor.hasNext() && keys.size() < limit) {
                String key = new String(cursor.next(), StandardCharsets.UTF_8);
                keys.add(key);
            }
        }
        catch (Exception e) {
            log.warn("PlaceHistoryCleaner ‑ SCAN failed : {}", e.getMessage(), e);
        }
        return keys;
    }
}
