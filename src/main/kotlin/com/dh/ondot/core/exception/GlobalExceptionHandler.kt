package com.dh.ondot.core.exception

import com.dh.ondot.core.ErrorResponse
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.ConstraintViolationException
import org.apache.catalina.connector.ClientAbortException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBindingException(e: BindException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.FIELD_ERROR, e.bindingResult)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolationException(e: ConstraintViolationException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.URL_PARAMETER_ERROR, e.constraintViolations)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingParam(e: MissingServletRequestParameterException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.URL_PARAMETER_ERROR)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleClientAbortException(e: ClientAbortException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.ALREADY_DISCONNECTED)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.INVALID_JSON)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequestException(e: BadRequestException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorizedException(e: UnauthorizedException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbiddenException(e: ForbiddenException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoResourceFoundException(e: NoResourceFoundException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.NO_RESOURCE_FOUND)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.METHOD_NOT_SUPPORTED)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflictException(e: ConflictException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    fun handleHttpMediaTypeNotSupportedException(e: HttpMediaTypeNotSupportedException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun handleTooManyRequestsException(e: TooManyRequestsException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternalServerErrorException(e: InternalServerException): ErrorResponse {
        log.error(e.message, e)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ErrorResponse {
        log.error(e.message, e)
        return ErrorResponse(ErrorCode.SERVER_ERROR)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun handleBadGatewayException(e: BadGatewayException): ErrorResponse {
        log.error(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleServiceUnavailableException(e: ServiceUnavailableException): ErrorResponse {
        log.error(e.message)
        return ErrorResponse(e)
    }
}
