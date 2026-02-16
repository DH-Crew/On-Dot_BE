package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.ErrorCode
import com.dh.ondot.core.exception.InternalServerException

class SerializationException(private val error: ErrorCode) :
    InternalServerException(error.message) {
    override val errorCode: String get() = error.name
}
