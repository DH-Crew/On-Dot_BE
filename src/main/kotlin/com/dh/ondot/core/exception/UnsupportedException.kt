package com.dh.ondot.core.exception

class UnsupportedException(
    private val error: ErrorCode,
    obj: String,
) : BadRequestException(error.message.format(obj)) {
    override val errorCode: String get() = error.name
}
