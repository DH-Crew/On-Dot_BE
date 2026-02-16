package com.dh.ondot.notification.domain

import java.time.LocalDateTime

data class AlertIssue(
    val type: AlertType,
    val message: String,
    val occurredAt: LocalDateTime,
)
