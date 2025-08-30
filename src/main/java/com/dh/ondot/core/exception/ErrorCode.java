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
    SERVER_ERROR(INTERNAL_SERVER_ERROR,"서버 오류가 발생했습니다. 관리자에게 문의해주세요."),
    REDIS_UNAVAILABLE(SERVICE_UNAVAILABLE,"Redis 서버에 연결할 수 없습니다."),
    EVENT_SERIALIZATION_FAILED(INTERNAL_SERVER_ERROR, "이벤트 직렬화 중 오류가 발생했습니다."),

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
    NOT_FOUND_MEMBER(NOT_FOUND, "회원을 찾을 수 없습니다. MemberId : %d"),
    ALREADY_ONBOARDED_MEMBER(CONFLICT, "이미 온보딩을 완료한 회원입니다. MemberId : %d"),
    NOT_FOUND_QUESTION(NOT_FOUND, "질문을 찾을 수 없습니다. QuestionId : %d"),
    NOT_FOUND_ANSWER(NOT_FOUND, "답을 찾을 수 없습니다. AnswerId : %d"),
    NOT_FOUND_ADDRESS(NOT_FOUND, "회원이 저장한 주소를 찾을 수 없습니다. MemberId : %d"),
    UNSUPPORTED_MAP_PROVIDER(BAD_REQUEST, "지원하지 않는 지도 제공자입니다. MapProvider : %s"),
    UNSUPPORTED_ADDRESS_TYPE(BAD_REQUEST, "지원하지 않는 주소 타입입니다. AddressType : %s"),

    // Alarm
    UNSUPPORTED_ALARM_MODE(BAD_REQUEST, "지원하지 않는 알람 모드입니다. Mode : %s"),
    UNSUPPORTED_RING_TONE(BAD_REQUEST, "지원하지 않는 벨소리입니다. RingTone : %s"),
    UNSUPPORTED_SNOOZE_INTERVAL(BAD_REQUEST, "지원하지 않는 알람 미루기 간격입니다. SnoozeInterval : %s"),
    UNSUPPORTED_SNOOZE_COUNT(BAD_REQUEST, "지원하지 않는 알람 미루기 횟수입니다. SnoozeCount : %s"),
    UNSUPPORTED_SOUND_CATEGORY(BAD_REQUEST, "지원하지 않는 사운드 카테고리입니다. SoundCategory : %s"),
    UNSUPPORTED_MISSION(BAD_REQUEST, "지원하지 않는 미션입니다. Mission : %s"),
    NOT_FOUND_SCHEDULE(NOT_FOUND, "일정을 찾을 수 없습니다. ScheduleId : %d"),

    // Place
    PLACE_HISTORY_SERIALIZATION_FAILED(INTERNAL_SERVER_ERROR, "장소 검색 기록 직렬화 중 오류가 발생했습니다."),

    // AI
    AI_USAGE_LIMIT_EXCEEDED(TOO_MANY_REQUESTS, "오늘 사용 가능한 AI 사용 횟수를 초과했습니다. MemberId : %d, Date : %s"),
    OPEN_AI_PARSING_ERROR(BAD_REQUEST, "약속 문장을 이해할 수 없습니다. 형식을 확인 후 다시 시도해주세요."),
    UNAVAILABLE_OPEN_AI_SERVER(BAD_GATEWAY, "일시적으로 Open AI 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."),
    UNHANDLED_OPEN_AI(INTERNAL_SERVER_ERROR, "Open AI 요청 과정에서 알 수 없는 문제가 발생했습니다. 관리자에게 문의해주세요."),

    // ODsay API
    ODSAY_BAD_INPUT(BAD_REQUEST, "필수 입력값 형식 및 범위를 확인해주세요: %s"),
    ODSAY_MISSING_PARAM(BAD_REQUEST, "필수 입력값이 누락되었습니다: %s"),
    ODSAY_NO_STOP(BAD_REQUEST, "출발지 또는 도착지 정류장을 찾을 수 없습니다: %s"),
    ODSAY_SERVICE_AREA(BAD_REQUEST, "서비스 지역이 아닙니다: %s"),
    ODSAY_TOO_CLOSE(BAD_REQUEST, "출발지와 도착지가 너무 가깝습니다: %s"),
    ODSAY_NO_RESULT(NOT_FOUND, "검색 결과가 없습니다: %s"),
    ODSAY_SERVER_ERROR(BAD_GATEWAY, "ODSay 서버 내부 오류가 발생했습니다: %s"),
    ODSAY_UNHANDLED_ERROR(INTERNAL_SERVER_ERROR, "ODSay API 처리 중 알 수 없는 오류가 발생했습니다: %s"),
    ;

    public final HttpStatus httpStatus;
    private final String message;
}
