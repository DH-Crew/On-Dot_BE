package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.ErrorCode.ODSAY_USAGE_LIMIT_EXCEEDED
import com.dh.ondot.core.exception.ForbiddenException
import java.time.LocalDate

class MaxOdsayUsageLimitExceededException(usageDate: LocalDate) :
    ForbiddenException(ODSAY_USAGE_LIMIT_EXCEEDED.message.format(usageDate)) {
    override val errorCode: String get() = ODSAY_USAGE_LIMIT_EXCEEDED.name
}
