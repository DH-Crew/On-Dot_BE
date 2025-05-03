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

    public int calculateRouteTime(Double startX, Double startY,
                                  Double endX,   Double endY) {

        OdsayRouteApiResponse res =
                odayPathApi.searchPublicTransportRoute(startX, startY, endX, endY);

        // Adjust total time for each path
        List<Double> adjustedTimes = res.result()
                .path().stream()
                .map(path -> {

                    double adjusted = path.info().totalTime();  // base time

                    // Transfer adjustment
                    long publicLegs = path.subPath().stream()
                            .filter(sp -> sp.trafficType() == 1 || sp.trafficType() == 2)
                            .count();
                    long transferCnt = Math.max(0, publicLegs - 1);
                    adjusted += transferCnt * 6.5;

                    // Long walking adjustment
                    long longWalks = path.subPath().stream()
                            .filter(sp -> sp.trafficType() == 3 && sp.distance() > 800)
                            .count();
                    adjusted += longWalks * 4.0;

                    return adjusted;
                })
                .sorted()
                .limit(3)
                .toList();

        // Calculate average of top 3 paths
        double avg = adjustedTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        // Add buffer and round
        return (int) Math.round(avg + 10);
    }
}
