package com.dh.ondot.member.application.command

import com.dh.ondot.member.api.request.OnboardingRequest

data class CreateAddressCommand(
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
) {
    companion object {
        fun from(request: OnboardingRequest): CreateAddressCommand =
            CreateAddressCommand(
                roadAddress = request.roadAddress,
                longitude = request.longitude,
                latitude = request.latitude,
            )
    }
}
