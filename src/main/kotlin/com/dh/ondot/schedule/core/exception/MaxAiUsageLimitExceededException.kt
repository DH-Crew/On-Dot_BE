package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.ErrorCode.AI_USAGE_LIMIT_EXCEEDED
import com.dh.ondot.core.exception.TooManyRequestsException
import java.time.LocalDate

class MaxAiUsageLimitExceededException(memberId: Long, date: LocalDate) :
    TooManyRequestsException(AI_USAGE_LIMIT_EXCEEDED.message.format(memberId, date.toString())) {
    override val errorCode: String get() = AI_USAGE_LIMIT_EXCEEDED.name
}
