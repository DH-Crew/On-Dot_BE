package com.dh.ondot.member.application.command

data class OnboardingCommand(
    val preparationTime: Int,
    val alarmMode: String,
    val isSnoozeEnabled: Boolean,
    val snoozeInterval: Int,
    val snoozeCount: Int,
    val soundCategory: String,
    val ringTone: String,
    val volume: Double,
)
