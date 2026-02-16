package com.dh.ondot.member.presentation.request

import com.dh.ondot.member.application.command.CreateAddressCommand
import com.dh.ondot.member.application.command.CreateChoicesCommand
import com.dh.ondot.member.application.command.OnboardingCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class OnboardingRequest(
    @field:NotNull @field:Min(1) @field:Max(600)
    val preparationTime: Int,

    @field:NotBlank
    val roadAddress: String,

    @field:NotNull @field:DecimalMin("-180.0") @field:DecimalMax("180.0")
    val longitude: Double,

    @field:NotNull @field:DecimalMin("-90.0") @field:DecimalMax("90.0")
    val latitude: Double,

    @field:NotBlank
    val alarmMode: String,

    @field:NotNull
    val isSnoozeEnabled: Boolean,

    @field:NotNull @field:Min(1) @field:Max(60)
    val snoozeInterval: Int,

    @field:NotNull @field:Min(-1) @field:Max(10)
    val snoozeCount: Int,

    @field:NotBlank
    val soundCategory: String,

    @field:NotBlank
    val ringTone: String,

    @field:NotNull @field:Min(0) @field:Max(1)
    val volume: Double,

    @field:NotNull @field:Size(min = 1) @field:Valid
    val questions: List<@Valid QuestionDto>,
) {
    data class QuestionDto(
        @field:NotNull val questionId: Long,
        @field:NotNull val answerId: Long,
    )

    fun toOnboardingCommand(): OnboardingCommand =
        OnboardingCommand(
            preparationTime = preparationTime,
            alarmMode = alarmMode,
            isSnoozeEnabled = isSnoozeEnabled,
            snoozeInterval = snoozeInterval,
            snoozeCount = snoozeCount,
            soundCategory = soundCategory,
            ringTone = ringTone,
            volume = volume,
        )

    fun toAddressCommand(): CreateAddressCommand =
        CreateAddressCommand(
            roadAddress = roadAddress,
            longitude = longitude,
            latitude = latitude,
        )

    fun toChoicesCommand(): CreateChoicesCommand =
        CreateChoicesCommand(
            questionAnswerPairs = questions.map { q ->
                CreateChoicesCommand.QuestionAnswerPair(q.questionId, q.answerId)
            }
        )
}
