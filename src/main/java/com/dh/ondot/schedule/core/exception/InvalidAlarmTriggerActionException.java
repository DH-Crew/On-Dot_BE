package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.INVALID_ALARM_TRIGGER_ACTION;

public class InvalidAlarmTriggerActionException extends BadRequestException {
    public InvalidAlarmTriggerActionException(String value) {
        super(INVALID_ALARM_TRIGGER_ACTION.getMessage().formatted(value));
    }

    @Override
    public String getErrorCode() {
        return INVALID_ALARM_TRIGGER_ACTION.name();
    }
}
