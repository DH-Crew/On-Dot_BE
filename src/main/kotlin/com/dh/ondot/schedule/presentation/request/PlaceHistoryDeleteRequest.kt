package com.dh.ondot.schedule.presentation.request

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class PlaceHistoryDeleteRequest(
    @field:NotNull(message = "검색 시각은 필수입니다.")
    val searchedAt: LocalDateTime,
)
