package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_ALARM;

public class NotFoundAlarmException extends NotFoundException {
    public NotFoundAlarmException(Long alarmId) {
        super(NOT_FOUND_ALARM.getMessage().formatted(alarmId));
    }

    @Override
    public String getErrorCode() {
        return NOT_FOUND_ALARM.name();
    }
}
