package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.ForbiddenException;

import java.time.LocalDate;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_USAGE_LIMIT_EXCEEDED;

public class MaxOdsayUsageLimitExceededException extends ForbiddenException {
    public MaxOdsayUsageLimitExceededException(LocalDate usageDate) {
        super(ODSAY_USAGE_LIMIT_EXCEEDED.getMessage().formatted(usageDate));
    }

    @Override
    public String getErrorCode() {
        return ODSAY_USAGE_LIMIT_EXCEEDED.name();
    }
}
