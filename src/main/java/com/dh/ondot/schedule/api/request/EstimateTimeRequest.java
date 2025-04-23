package com.dh.ondot.schedule.api.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record EstimateTimeRequest(
        @NotNull(message = "startLongitude는 필수입니다.")
        @DecimalMin(value = "-180.0", message = "startLongitude는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "startLongitude는 180 이하이어야 합니다.")
        Double startLongitude,

        @NotNull(message = "startLatitude는 필수입니다.")
        @DecimalMin(value = "-90.0", message = "startLatitude는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "startLatitude는 90 이하이어야 합니다.")
        Double startLatitude,

        @NotNull(message = "endLongitude는 필수입니다.")
        @DecimalMin(value = "-180.0", message = "endLongitude는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "endLongitude는 180 이하이어야 합니다.")
        Double endLongitude,

        @NotNull(message = "endLatitude는 필수입니다.")
        @DecimalMin(value = "-90.0", message = "endLatitude는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "endLatitude는 90 이하이어야 합니다.")
        Double endLatitude
) {
}
