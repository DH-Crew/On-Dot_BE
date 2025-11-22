package com.dh.ondot.member.application.command;

import com.dh.ondot.member.api.request.OnboardingRequest;

public record CreateAddressCommand(
        String roadAddress,
        Double longitude,
        Double latitude
) {
    public static CreateAddressCommand from(OnboardingRequest request) {
        return new CreateAddressCommand(
                request.roadAddress(),
                request.longitude(),
                request.latitude()
        );
    }
}
