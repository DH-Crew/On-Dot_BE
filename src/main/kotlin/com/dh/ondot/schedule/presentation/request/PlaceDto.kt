package com.dh.ondot.schedule.presentation.request

import com.dh.ondot.schedule.domain.Place
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PlaceDto(
    @field:NotBlank
    val title: String,

    @field:NotBlank
    val roadAddress: String,

    @field:NotNull @field:DecimalMin("-180.0") @field:DecimalMax("180.0")
    val longitude: Double,

    @field:NotNull @field:DecimalMin("-90.0") @field:DecimalMax("90.0")
    val latitude: Double,
) {
    companion object {
        fun from(place: Place): PlaceDto {
            return PlaceDto(place.title, place.roadAddress, place.longitude, place.latitude)
        }
    }
}
