package com.dh.ondot.member.api.request;

import java.util.List;

public record OnboardingRequest(
        int preparationTime,
        String addressTitle,
        double longitude,
        double latitude,
        String ringTone,
        int volume,
        List<QuestionDto> questions
) {
    public record QuestionDto(
            Long questionId,
            String answer
    ) {
    }
}
