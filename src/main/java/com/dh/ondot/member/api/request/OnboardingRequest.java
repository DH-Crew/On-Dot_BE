package com.dh.ondot.member.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OnboardingRequest(
        @NotNull @Min(1) @Max(600) int preparationTime,

        @NotBlank String roadAddress,

        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,

        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,

        @NotBlank String alarmMode,

        @NotNull Boolean isSnoozeEnabled,

        @NotNull @Min(1) @Max(60) Integer snoozeInterval,

        @NotNull @Min(-1) @Max(10) Integer snoozeCount,

        @NotBlank String soundCategory,

        @NotBlank String ringTone,

        @NotNull @Min(0) @Max(1) Double volume,

        @NotNull @Size(min = 1) @Valid
        List<@Valid QuestionDto> questions
) {
    public record QuestionDto(
            @NotNull Long questionId,

            @NotNull Long answerId
    ) {
    }
}
