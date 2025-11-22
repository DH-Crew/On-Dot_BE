package com.dh.ondot.member.application.command;

import com.dh.ondot.member.api.request.OnboardingRequest;

public record OnboardingCommand(
        Integer preparationTime,
        String alarmMode,
        Boolean isSnoozeEnabled,
        Integer snoozeInterval,
        Integer snoozeCount,
        String soundCategory,
        String ringTone,
        Double volume
) {
    public static OnboardingCommand from(OnboardingRequest request) {
        return new OnboardingCommand(
                request.preparationTime(),
                request.alarmMode(),
                request.isSnoozeEnabled(),
                request.snoozeInterval(),
                request.snoozeCount(),
                request.soundCategory(),
                request.ringTone(),
                request.volume()
        );
    }
}
