package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.core.util.GeoUtils;
import com.dh.ondot.schedule.infra.api.OdsayPathApi;
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse;
import com.dh.ondot.schedule.infra.exception.OdsayTooCloseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    
    private static final double TRANSFER_PENALTY_MINUTES = 6.5;
    private static final double LONG_WALK_PENALTY_MINUTES = 4.0;
    private static final int LONG_WALK_DISTANCE_THRESHOLD = 800;
    private static final int BUFFER_TIME_MINUTES = 10;
    private static final int TOP_ROUTES_LIMIT = 3;
    private static final double WALKING_SPEED_MPS = 1.25;
    private static final int SUBWAY_TRAFFIC_TYPE = 1;
    private static final int BUS_TRAFFIC_TYPE = 2;
    private static final int WALKING_TRAFFIC_TYPE = 3;
    
    private final OdsayPathApi odayPathApi;
    private final OdsayUsageService odsayUsageService;

    public int calculateRouteTime(Double startX, Double startY, Double endX, Double endY) {
        checkApiUsageLimit();
        OdsayRouteApiResponse response = getRouteTimeFromApi(startX, startY, endX, endY);
        return calculateFinalTravelTime(response);
    }

    private void checkApiUsageLimit() {
        odsayUsageService.checkAndIncrementUsage();
    }

    private OdsayRouteApiResponse getRouteTimeFromApi(Double startX, Double startY, Double endX, Double endY) {
        try {
            return odayPathApi.searchPublicTransportRoute(startX, startY, endX, endY);
        } catch (OdsayTooCloseException e) {
            int walkTime = calculateWalkTime(startX, startY, endX, endY);
            return OdsayRouteApiResponse.walkOnly(walkTime);
        }
    }

    private int calculateWalkTime(double startX, double startY, double endX, double endY) {
        double distanceInMeters = GeoUtils.calculateDistance(startX, startY, endX, endY);
        return convertDistanceToWalkingTime(distanceInMeters);
    }

    private int calculateFinalTravelTime(OdsayRouteApiResponse response) {
        List<Double> adjustedTimes = calculateAdjustedTimesForAllPaths(response);
        double averageTime = calculateAverageOfTopRoutes(adjustedTimes);
        return addBufferTimeAndRound(averageTime);
    }

    /**
     * 모든 경로에 대해 보정된 시간 계산
     */
    private List<Double> calculateAdjustedTimesForAllPaths(OdsayRouteApiResponse response) {
        return response.result()
                .path().stream()
                .map(this::calculateAdjustedTimeForSinglePath)
                .sorted()
                .limit(TOP_ROUTES_LIMIT)
                .toList();
    }

    /**
     * 단일 경로에 대한 시간 보정 계산
     */
    private double calculateAdjustedTimeForSinglePath(OdsayRouteApiResponse.Path path) {
        double baseTime = path.info().totalTime();
        double transferPenalty = calculateTransferPenalty(path);
        double longWalkPenalty = calculateLongWalkPenalty(path);
        
        return baseTime + transferPenalty + longWalkPenalty;
    }

    /**
     * 환승 시간 페널티 계산 (환승 1회당 6.5분 추가)
     */
    private double calculateTransferPenalty(OdsayRouteApiResponse.Path path) {
        long publicTransportLegs = countPublicTransportLegs(path);
        long transferCount = Math.max(0, publicTransportLegs - 1);
        return transferCount * TRANSFER_PENALTY_MINUTES;
    }

    /**
     * 긴 도보 구간 페널티 계산 (800m 초과 도보당 4분 추가)
     */
    private double calculateLongWalkPenalty(OdsayRouteApiResponse.Path path) {
        long longWalkCount = countLongWalkSegments(path);
        return longWalkCount * LONG_WALK_PENALTY_MINUTES;
    }

    /**
     * 대중교통 구간 개수 계산 (지하철, 버스)
     */
    private long countPublicTransportLegs(OdsayRouteApiResponse.Path path) {
        return path.subPath().stream()
                .filter(sp -> sp.trafficType() == SUBWAY_TRAFFIC_TYPE || sp.trafficType() == BUS_TRAFFIC_TYPE)
                .count();
    }

    /**
     * 긴 도보 구간 개수 계산 (800m 초과)
     */
    private long countLongWalkSegments(OdsayRouteApiResponse.Path path) {
        return path.subPath().stream()
                .filter(sp -> sp.trafficType() == WALKING_TRAFFIC_TYPE && sp.distance() > LONG_WALK_DISTANCE_THRESHOLD)
                .count();
    }

    /**
     * 상위 경로들의 평균 시간 계산
     */
    private double calculateAverageOfTopRoutes(List<Double> adjustedTimes) {
        return adjustedTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }

    /**
     * 여유시간 추가 후 반올림
     */
    private int addBufferTimeAndRound(double averageTime) {
        return (int) Math.round(averageTime + BUFFER_TIME_MINUTES);
    }

    /**
     * 거리를 도보 시간으로 변환
     */
    private int convertDistanceToWalkingTime(double distanceInMeters) {
        double timeInSeconds = distanceInMeters / WALKING_SPEED_MPS;
        double timeInMinutes = timeInSeconds / 60;
        return (int) Math.round(timeInMinutes);
    }
}
