package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_MEMBER
import com.dh.ondot.core.exception.NotFoundException

class NotFoundMemberException(memberId: Long) :
    NotFoundException(NOT_FOUND_MEMBER.message.format(memberId)) {
    override val errorCode: String get() = NOT_FOUND_MEMBER.name
}
