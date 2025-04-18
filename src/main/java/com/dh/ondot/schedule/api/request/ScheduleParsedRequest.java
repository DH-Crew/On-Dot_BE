package com.dh.ondot.schedule.api.request;

import jakarta.validation.constraints.NotBlank;

public record ScheduleParsedRequest(
        @NotBlank String text
) {
}
