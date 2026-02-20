package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.ErrorCode.API_USAGE_LIMIT_EXCEEDED
import com.dh.ondot.core.exception.ForbiddenException
import java.time.LocalDate

class MaxApiUsageLimitExceededException(apiTypeName: String, usageDate: LocalDate) :
    ForbiddenException(API_USAGE_LIMIT_EXCEEDED.message.format(apiTypeName, usageDate)) {
    override val errorCode: String get() = API_USAGE_LIMIT_EXCEEDED.name
}
