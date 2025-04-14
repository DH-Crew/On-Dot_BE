package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_SCHEDULE;

public class NotFoundScheduleException extends NotFoundException {
    public NotFoundScheduleException(Long scheduleId) {
        super(NOT_FOUND_SCHEDULE.getMessage().formatted(scheduleId));
    }

    @Override
    public String getErrorCode() {
        return NOT_FOUND_SCHEDULE.name();
    }
}
