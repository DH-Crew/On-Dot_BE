package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ConflictException
import com.dh.ondot.core.exception.ErrorCode.ALREADY_ONBOARDED_MEMBER

class AlreadyOnboardedException(memberId: Long) :
    ConflictException(ALREADY_ONBOARDED_MEMBER.message.format(memberId)) {
    override val errorCode: String get() = ALREADY_ONBOARDED_MEMBER.name
}
