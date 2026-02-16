package com.dh.ondot.member.application.command

import com.dh.ondot.member.api.request.OnboardingRequest

data class OnboardingCommand(
    val preparationTime: Int,
    val alarmMode: String,
    val isSnoozeEnabled: Boolean,
    val snoozeInterval: Int,
    val snoozeCount: Int,
    val soundCategory: String,
    val ringTone: String,
    val volume: Double,
) {
    companion object {
        fun from(request: OnboardingRequest): OnboardingCommand =
            OnboardingCommand(
                preparationTime = request.preparationTime(),
                alarmMode = request.alarmMode(),
                isSnoozeEnabled = request.isSnoozeEnabled(),
                snoozeInterval = request.snoozeInterval(),
                snoozeCount = request.snoozeCount(),
                soundCategory = request.soundCategory(),
                ringTone = request.ringTone(),
                volume = request.volume(),
            )
    }
}
