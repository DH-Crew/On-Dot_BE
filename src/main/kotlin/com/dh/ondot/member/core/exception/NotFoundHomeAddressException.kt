package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_HOME_ADDRESS
import com.dh.ondot.core.exception.NotFoundException

class NotFoundHomeAddressException(memberId: Long) :
    NotFoundException(NOT_FOUND_HOME_ADDRESS.message.format(memberId)) {
    override val errorCode: String get() = NOT_FOUND_HOME_ADDRESS.name
}
