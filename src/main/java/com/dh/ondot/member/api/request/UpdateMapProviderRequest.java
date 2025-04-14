package com.dh.ondot.member.api.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateMapProviderRequest(
        @NotBlank String mapProvider
) {
}
