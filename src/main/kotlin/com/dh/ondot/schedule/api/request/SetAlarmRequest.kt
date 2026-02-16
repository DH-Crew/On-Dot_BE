package com.dh.ondot.schedule.api.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class SetAlarmRequest(
    @field:NotNull
    val appointmentAt: LocalDateTime,

    @field:NotNull(message = "startLongitude는 필수입니다.")
    @field:DecimalMin(value = "-180.0", message = "startLongitude는 -180 이상이어야 합니다.")
    @field:DecimalMax(value = "180.0", message = "startLongitude는 180 이하이어야 합니다.")
    val startLongitude: Double,

    @field:NotNull(message = "startLatitude는 필수입니다.")
    @field:DecimalMin(value = "-90.0", message = "startLatitude는 -90 이상이어야 합니다.")
    @field:DecimalMax(value = "90.0", message = "startLatitude는 90 이하이어야 합니다.")
    val startLatitude: Double,

    @field:NotNull(message = "endLongitude는 필수입니다.")
    @field:DecimalMin(value = "-180.0", message = "endLongitude는 -180 이상이어야 합니다.")
    @field:DecimalMax(value = "180.0", message = "endLongitude는 180 이하이어야 합니다.")
    val endLongitude: Double,

    @field:NotNull(message = "endLatitude는 필수입니다.")
    @field:DecimalMin(value = "-90.0", message = "endLatitude는 -90 이상이어야 합니다.")
    @field:DecimalMax(value = "90.0", message = "endLatitude는 90 이하이어야 합니다.")
    val endLatitude: Double,
)
