package com.dh.ondot.notification.presentation.request

import jakarta.validation.constraints.NotBlank

data class DeleteDeviceTokenRequest(
    @field:NotBlank
    val fcmToken: String,
)
