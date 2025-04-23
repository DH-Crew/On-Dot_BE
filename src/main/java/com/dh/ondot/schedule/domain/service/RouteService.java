package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.schedule.infra.OdsayPathApi;
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final OdsayPathApi odayPathApi;

    public int calculateRouteTime(Double startX, Double startY, Double endX, Double endY) {
        OdsayRouteApiResponse response = odayPathApi.searchPublicTransportRoute(startX, startY, endX, endY);

        List<Integer> topTimes = response.result()
                .path().stream()
                .map(p -> p.info().totalTime())
                .limit(3)
                .toList();

        double avg = topTimes.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        return (int) Math.round(avg);
    }
}
