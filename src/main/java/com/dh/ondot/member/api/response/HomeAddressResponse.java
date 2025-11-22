package com.dh.ondot.member.api.response;

import com.dh.ondot.member.domain.Address;

public record HomeAddressResponse(
        String roadAddress,
        Double longitude,
        Double latitude
) {
    public static HomeAddressResponse from(Address address) {
        return new HomeAddressResponse(
                address.getRoadAddress(),
                address.getLongitude(),
                address.getLatitude()
        );
    }
}
