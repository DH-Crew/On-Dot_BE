package com.dh.ondot.schedule.infra.dto;

import java.util.List;

public record OdsayErrorResponse(
        List<Error> error
) {
    public record Error(
            String code,
            String message
    ) {}
}
