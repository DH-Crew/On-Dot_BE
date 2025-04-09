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
    REDIS_UNAVAILABLE(SERVICE_UNAVAILABLE,"Redis 서버에 연결할 수 없습니다."),

    // Token
    INVALID_TOKEN_HEADER(UNAUTHORIZED, "토큰 헤더 형식이 잘못되었습니다."),
    TOKEN_INVALID(UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 로그인해 주세요."),
    TOKEN_MISSING(UNAUTHORIZED, "토큰이 요청 헤더에 없습니다. 새로운 토큰을 재발급 받으세요"),
    TOKEN_BLACKLISTED(UNAUTHORIZED, "해당 토큰은 사용이 금지되었습니다. 다시 로그인해 주세요."),
    TOKEN_EXPIRED(UNAUTHORIZED, "토큰이 만료되었습니다. 새로운 토큰을 재발급 받으세요."),
    REFRESH_TOKEN_EXPIRED(UNAUTHORIZED, "리프레쉬 토큰이 만료되었습니다. 다시 로그인해 주세요."),

    // OAuth
    UNSUPPORTED_SOCIAL_LOGIN(BAD_REQUEST, "지원하지 않는 소셜 로그인 타입입니다. type : %s"),
    OAUTH_USER_FETCH_FAILED(SERVICE_UNAVAILABLE, "%s 사용자 정보를 가져오는 데 실패했습니다. 잠시 후 다시 시도해주세요."),
    APPLE_AUTHORIZATION_CODE_EXPIRED(UNAUTHORIZED, "애플 Authorization Code가 만료되었거나 잘못되었습니다. 다시 시도해주세요."),
    APPLE_SIGNATURE_INVALID(UNAUTHORIZED, "애플 id_token 서명 검증에 실패했습니다. 위조되었을 가능성이 있습니다."),
    APPLE_USER_PARSE_FAILED(NOT_FOUND, "애플 사용자 정보를 파싱하는 데 실패했습니다. id_token 구조를 확인하세요."),
    APPLE_PRIVATE_KEY_LOAD_FAILED(INTERNAL_SERVER_ERROR, "Apple 비공개 키 파일을 로드하는 데 실패했습니다. 파일 경로 또는 포맷을 확인하세요."),

    // Member
    MEMBER_NOT_FOUND(NOT_FOUND, "회원을 찾을 수 없습니다. memberId : %d"),
    UNSUPPORTED_MAP_PROVIDER(BAD_REQUEST, "지원하지 않는 지도 제공자입니다. mapProvider : %s"),

    // Alarm
    UNSUPPORTED_SOUND_MODE(BAD_REQUEST, "지원하지 않는 사운드 모드입니다. mode : %s"),
    UNSUPPORTED_RING_TONE(BAD_REQUEST, "지원하지 않는 벨소리입니다. ringTone : %s")
    ;

    public final HttpStatus httpStatus;
    private final String message;
}
