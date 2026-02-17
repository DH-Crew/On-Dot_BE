package com.dh.ondot.notification.presentation.request

import com.dh.ondot.notification.application.dto.RegisterDeviceTokenCommand
import jakarta.validation.constraints.NotBlank

data class RegisterDeviceTokenRequest(
    @field:NotBlank
    val fcmToken: String,

    @field:NotBlank
    val deviceType: String,
) {
    fun toCommand(memberId: Long): RegisterDeviceTokenCommand =
        RegisterDeviceTokenCommand(
            memberId = memberId,
            fcmToken = fcmToken,
            deviceType = deviceType,
        )
}
