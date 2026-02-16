package com.dh.ondot.notification.domain.dto

import java.time.LocalDateTime

data class EmergencyAlertDto(
    val content: String,
    val regionName: String,
    val createdAt: LocalDateTime,
)
