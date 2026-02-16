package com.dh.ondot.schedule.domain.event

import java.time.LocalDateTime

data class QuickScheduleRequestedEvent(
    val memberId: Long,
    val departurePlaceId: Long,
    val arrivalPlaceId: Long,
    val appointmentAt: LocalDateTime
)
