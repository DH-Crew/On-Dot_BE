package com.dh.ondot.member.api.response;

import com.dh.ondot.member.domain.Address;

public record UpdateHomeAddressResponse(
        String roadAddress,
        Double longitude,
        Double latitude
) {
    public static UpdateHomeAddressResponse from(Address address) {
        return new UpdateHomeAddressResponse(
                address.getRoadAddress(),
                address.getLongitude(),
                address.getLatitude()
        );
    }
}
