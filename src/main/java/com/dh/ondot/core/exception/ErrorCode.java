package com.dh.ondot.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_JSON(BAD_REQUEST,"잘못된 JSON 형식입니다. 요청 데이터를 확인하세요."),
    FIELD_ERROR(BAD_REQUEST,"입력이 잘못되었습니다."),
    URL_PARAMETER_ERROR(BAD_REQUEST,"입력이 잘못되었습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(BAD_REQUEST,"입력한 값의 타입이 잘못되었습니다."),
    ALREADY_DISCONNECTED(BAD_REQUEST,"이미 클라이언트에서 요청이 종료되었습니다."),
    NO_RESOURCE_FOUND(NOT_FOUND,"요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_SUPPORTED(METHOD_NOT_ALLOWED,"허용되지 않은 메서드입니다."),
    MEDIA_TYPE_NOT_SUPPORTED(UNSUPPORTED_MEDIA_TYPE,"허용되지 않은 미디어 타입입니다."),
    SERVER_ERROR(INTERNAL_SERVER_ERROR,"서버 오류가 발생했습니다. 관리자에게 문의하세요."),
    REDIS_UNAVAILABLE(INTERNAL_SERVER_ERROR,"Redis 서버에 연결할 수 없습니다.");

    public final HttpStatus httpStatus;
    private final String message;
}