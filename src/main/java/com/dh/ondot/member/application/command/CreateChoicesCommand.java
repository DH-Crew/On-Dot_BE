package com.dh.ondot.member.application.command;

import com.dh.ondot.member.api.request.OnboardingRequest;

import java.util.List;

public record CreateChoicesCommand(
        List<QuestionAnswerPair> questionAnswerPairs
) {
    public record QuestionAnswerPair(
            Long questionId,
            Long answerId
    ) {
    }
    
    public static CreateChoicesCommand from(OnboardingRequest request) {
        List<QuestionAnswerPair> pairs = request.questions().stream()
                .map(q -> new QuestionAnswerPair(q.questionId(), q.answerId()))
                .toList();
        return new CreateChoicesCommand(pairs);
    }
}
