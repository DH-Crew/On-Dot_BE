package com.dh.ondot.schedule.api.response;

public record EstimateTimeResponse(
        Integer estimatedTime
) {
    public static EstimateTimeResponse from(Integer estimatedTime) {
        return new EstimateTimeResponse(estimatedTime);
    }
}
