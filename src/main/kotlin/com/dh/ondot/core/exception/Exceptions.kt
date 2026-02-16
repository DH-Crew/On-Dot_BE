package com.dh.ondot.core.exception

abstract class BadRequestException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class UnauthorizedException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class ForbiddenException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class NotFoundException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class ConflictException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class TooManyRequestsException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class InternalServerException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class BadGatewayException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class ServiceUnavailableException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}
