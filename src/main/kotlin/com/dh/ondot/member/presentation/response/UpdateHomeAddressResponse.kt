package com.dh.ondot.member.presentation.response

import com.dh.ondot.member.domain.Address

data class UpdateHomeAddressResponse(
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
) {
    companion object {
        fun from(address: Address): UpdateHomeAddressResponse =
            UpdateHomeAddressResponse(
                roadAddress = address.roadAddress,
                longitude = address.longitude,
                latitude = address.latitude,
            )
    }
}
