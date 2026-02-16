package com.dh.ondot.schedule.domain.enums

import com.dh.ondot.schedule.core.exception.InvalidAlarmTriggerActionException

enum class AlarmTriggerAction(val value: String, val description: String) {
    SCHEDULED("scheduled", "스케줄링 등록"),
    STOP("stop", "알람 끔"),
    SNOOZE("snooze", "다시 알림"),
    VIEW_ROUTE("view_route", "경로안내 보기"),
    START_PREPARE("start_prepare", "준비 시작하기");

    companion object {
        @JvmStatic
        fun from(value: String): AlarmTriggerAction {
            if (value.isBlank()) {
                throw InvalidAlarmTriggerActionException("null or empty")
            }
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw InvalidAlarmTriggerActionException(value)
        }
    }
}
