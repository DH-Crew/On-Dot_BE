package com.dh.ondot.notification.domain.dto

import java.time.LocalDateTime

data class SubwayAlertDto(
    val title: String,
    val content: String,
    val lineName: String,
    val startAt: LocalDateTime,
    val createdAt: LocalDateTime,
)
