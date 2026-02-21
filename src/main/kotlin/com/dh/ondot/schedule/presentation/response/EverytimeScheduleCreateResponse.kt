package com.dh.ondot.schedule.presentation.response

data class EverytimeScheduleCreateResponse(
    val createdCount: Int,
    val schedules: List<EverytimeScheduleItem>,
) {
    data class EverytimeScheduleItem(
        val scheduleId: Long,
        val title: String,
        val repeatDays: List<Int>,
        val appointmentAt: String,
    )
}
