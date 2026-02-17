package com.dh.ondot.notification.application.dto

data class RegisterDeviceTokenCommand(
    val memberId: Long,
    val fcmToken: String,
    val deviceType: String,
)
