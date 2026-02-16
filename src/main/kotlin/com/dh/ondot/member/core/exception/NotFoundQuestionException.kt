package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_QUESTION
import com.dh.ondot.core.exception.NotFoundException

class NotFoundQuestionException(questionId: Long) :
    NotFoundException(NOT_FOUND_QUESTION.message.format(questionId)) {
    override val errorCode: String get() = NOT_FOUND_QUESTION.name
}
