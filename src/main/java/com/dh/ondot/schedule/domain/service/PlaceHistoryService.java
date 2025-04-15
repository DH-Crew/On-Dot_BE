package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.schedule.domain.PlaceHistory;
import com.dh.ondot.schedule.infra.redis.PlaceHistoryRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceHistoryService {

    private final PlaceHistoryRedisRepository repository;

    public void record(
            Long memberId, String title, String roadAddress,
            Double lon, Double lat
    ) {
        String finalTitle = (title == null || title.isBlank())
                ? roadAddress
                : title;

        repository.push(PlaceHistory.of(memberId, finalTitle, lon, lat));
    }

    public List<PlaceHistory> recent(Long memberId) {
        return repository.findRecent(memberId);
    }
}
