package com.dh.ondot.schedule.infra.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
     * 매 시간 정시에 실행하여, 여러 키를 랜덤으로 선정해 만료된 데이터를 삭제합니다.
     * 만약 샘플링한 키들 중 일정 비율(THRESHOLD) 이상에서 삭제가 발생하면
     * 최대 MAX_ITERATIONS 까지 추가 청소를 반복합니다.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanRandomKeys() {
        int iterations = 0;
        while(iterations < MAX_ITERATIONS) {
            List<String> keys = redisTemplate.keys(KEY_PREFIX + "*").stream().toList();
            if(keys.isEmpty()){
                log.debug("PlaceHistoryCleaner: No keys found. Exiting cleanup.");
                break;
            }

            int effectiveSample = Math.min(keys.size(), SAMPLE_SIZE);
            int cleanedCount = 0;
            for (int i = 0; i < effectiveSample; i++) {
                int idx = ThreadLocalRandom.current().nextInt(keys.size());
                String key = keys.get(idx);
                long removed = repository.deleteExpired(key);
                if (removed > 0) {
                    cleanedCount++;
                    log.debug("PlaceHistoryCleaner: key={} removed={}", key, removed);
                }
            }

            double ratio = (double) cleanedCount / effectiveSample;
            if(ratio < THRESHOLD) {
                break;
            }
            iterations++;
        }
    }
}
