package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.OPEN_AI_PARSING_ERROR

class OpenAiParsingException :
    BadRequestException(OPEN_AI_PARSING_ERROR.message) {
    override val errorCode: String get() = OPEN_AI_PARSING_ERROR.name
}
