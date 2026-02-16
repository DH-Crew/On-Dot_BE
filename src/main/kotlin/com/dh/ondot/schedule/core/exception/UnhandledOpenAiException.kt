package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.ErrorCode.UNHANDLED_OPEN_AI
import com.dh.ondot.core.exception.InternalServerException

class UnhandledOpenAiException :
    InternalServerException(UNHANDLED_OPEN_AI.message) {
    override val errorCode: String get() = UNHANDLED_OPEN_AI.name
}
