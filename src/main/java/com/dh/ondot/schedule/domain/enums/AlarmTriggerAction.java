package com.dh.ondot.schedule.domain.enums;

import com.dh.ondot.schedule.core.exception.InvalidAlarmTriggerActionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AlarmTriggerAction {
    SCHEDULED("scheduled", "스케줄링 등록"),
    STOP("stop", "알람 끔"),
    SNOOZE("snooze", "다시 알림"),
    VIEW_ROUTE("view_route", "경로안내 보기"),
    START_PREPARE("start_prepare", "준비 시작하기");

    private final String value;
    private final String description;

    public static AlarmTriggerAction from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidAlarmTriggerActionException("null or empty");
        }

        return Arrays.stream(values())
                .filter(action -> action.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new InvalidAlarmTriggerActionException(value));
    }
}
