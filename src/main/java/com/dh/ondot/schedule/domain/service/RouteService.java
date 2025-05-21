package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.core.util.GeoUtils;
import com.dh.ondot.schedule.infra.OdsayPathApi;
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse;
import com.dh.ondot.schedule.infra.exception.OdsayTooCloseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final OdsayPathApi odayPathApi;

    public int calculateRouteTime(
            Double startX, Double startY,
            Double endX,   Double endY
    ) {
        OdsayRouteApiResponse res;
        try {
            res = odayPathApi.searchPublicTransportRoute(startX, startY, endX, endY);
        } catch (OdsayTooCloseException e) {
            int walkTime = calculateWalkTime(startY, startX, endY, endX);
            res = OdsayRouteApiResponse.walkOnly(walkTime);
        }

        // Adjust total time for each path
        List<Double> adjustedTimes = res.result()
                .path().stream()
                .map(path -> {
                    double adjusted = path.info().totalTime(); // base time

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

        // Add buffer(10) and round
        return (int) Math.round(avg + 10);
    }

    public int calculateWalkTime(double startX, double startY, double endX, double endY) {
        double distance = GeoUtils.calculateDistance(startX, startY, endX, endY); // meters
        double walkingSpeedMps = 1.25;
        return (int) Math.round(distance / walkingSpeedMps / 60);
    }
}
