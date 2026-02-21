package com.dh.ondot.core

import com.dh.ondot.core.exception.*
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.ConstraintViolation
import org.springframework.validation.BindingResult

@Schema(description = "API 에러 응답")
@JsonInclude(Include.NON_NULL)
data class ErrorResponse(
    @Schema(description = "에러 코드", example = "TODO_LIMIT_EXCEEDED")
    val errorCode: String,

    @Schema(description = "에러 메시지", example = "오늘 할 일은 최대 3개까지 추가할 수 있습니다.")
    val message: String,

    @Schema(description = "필드 오류 목록 (있을 경우)")
    val fieldErrors: List<FieldError>? = null,

    @Schema(description = "제약 조건 위반 오류 목록 (있을 경우)")
    val violationErrors: List<ConstraintViolationError>? = null,
) {

    constructor(e: BadRequestException) : this(e.errorCode, e.message ?: "")
    constructor(e: UnauthorizedException) : this(e.errorCode, e.message ?: "")
    constructor(e: ForbiddenException) : this(e.errorCode, e.message ?: "")
    constructor(e: NotFoundException) : this(e.errorCode, e.message ?: "")
    constructor(e: ConflictException) : this(e.errorCode, e.message ?: "")
    constructor(e: TooManyRequestsException) : this(e.errorCode, e.message ?: "")
    constructor(e: InternalServerException) : this(e.errorCode, e.message ?: "")
    constructor(e: BadGatewayException) : this(e.errorCode, e.message ?: "")
    constructor(e: ServiceUnavailableException) : this(e.errorCode, e.message ?: "")

    constructor(errorCode: ErrorCode) : this(errorCode.name, errorCode.message)

    constructor(errorCode: ErrorCode, bindingResult: BindingResult) : this(
        errorCode = errorCode.name,
        message = errorCode.message,
        fieldErrors = FieldError.from(bindingResult),
    )

    constructor(errorCode: ErrorCode, constraintViolations: Set<ConstraintViolation<*>>) : this(
        errorCode = errorCode.name,
        message = errorCode.message,
        violationErrors = ConstraintViolationError.from(constraintViolations),
    )

    data class FieldError(
        val field: String,
        val rejectedValue: Any?,
        val reason: String?,
    ) {
        companion object {
            fun from(bindingResult: BindingResult): List<FieldError> =
                bindingResult.fieldErrors.map { e ->
                    FieldError(
                        field = e.field,
                        rejectedValue = e.rejectedValue,
                        reason = e.defaultMessage,
                    )
                }
        }
    }

    data class ConstraintViolationError(
        val field: String,
        val rejectedValue: Any?,
        val reason: String?,
    ) {
        companion object {
            private const val FIELD_POSITION = 1

            fun from(violations: Set<ConstraintViolation<*>>): List<ConstraintViolationError> =
                violations.map { v ->
                    ConstraintViolationError(
                        field = v.propertyPath.toString().split(".")[FIELD_POSITION],
                        rejectedValue = v.invalidValue?.toString(),
                        reason = v.message,
                    )
                }
        }
    }
}
