package com.dh.ondot.member.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateHomeAddressRequest(
        @NotBlank String roadAddress,
        @NotNull Double longitude,
        @NotNull Double latitude
) {
}
