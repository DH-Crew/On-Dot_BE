package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_ANSWER
import com.dh.ondot.core.exception.NotFoundException

class NotFoundAnswerException(answerId: Long) :
    NotFoundException(NOT_FOUND_ANSWER.message.format(answerId)) {
    override val errorCode: String get() = NOT_FOUND_ANSWER.name
}
