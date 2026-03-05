package com.dh.ondot.schedule.presentation.swagger

import com.dh.ondot.core.ErrorResponse
import com.dh.ondot.schedule.presentation.request.AlarmSwitchRequest
import com.dh.ondot.schedule.presentation.request.EstimateTimeRequest
import com.dh.ondot.schedule.presentation.request.EverytimeScheduleCreateRequest
import com.dh.ondot.schedule.presentation.request.EverytimeValidateRequest
import com.dh.ondot.schedule.presentation.request.QuickScheduleCreateRequest
import com.dh.ondot.schedule.presentation.request.ScheduleCreateRequest
import com.dh.ondot.schedule.presentation.request.ScheduleParsedRequest
import com.dh.ondot.schedule.presentation.request.ScheduleUpdateRequest
import com.dh.ondot.schedule.presentation.response.AlarmSwitchResponse
import com.dh.ondot.schedule.presentation.response.EstimateTimeResponse
import com.dh.ondot.schedule.presentation.response.EverytimeScheduleCreateResponse
import com.dh.ondot.schedule.presentation.response.EverytimeValidateResponse
import com.dh.ondot.schedule.presentation.response.HomeScheduleListResponse
import com.dh.ondot.schedule.presentation.response.ScheduleCreateResponse
import com.dh.ondot.schedule.presentation.response.ScheduleDetailResponse
import com.dh.ondot.schedule.presentation.response.ScheduleParsedResponse
import com.dh.ondot.schedule.presentation.response.SchedulePreparationResponse
import com.dh.ondot.schedule.presentation.response.ScheduleUpdateResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(
    name = "Schedule API",
    description = """
        **AccessToken (Authorization: Bearer JWT)** 은 필수값입니다.
        → 각 엔드포인트에 별도 파라미터는 필요 없으나 오른쪽 상단 Authorize에 토큰 값을 넣어야합니다.

        **🗓 repeatDays 요일 규칙**
        - 1 = 일요일
        - 2 = 월요일 …
        - 7 = 토요일

        **🔔 Alarm ENUM**
        - `AlarmMode`: SILENT, VIBRATE, SOUND
        - `SnoozeInterval`: 1, 3, 5, 10, 30, 60 (분)
        - `SnoozeCount`: -1(INFINITE), 1, 3, 5, 10 (회)
        - `SoundCategory`: *BRIGHT_ENERGY, FAST_INTENSE*
        - `RingTone`: *DANCING_IN_THE_STARDUST, IN_THE_CITY_LIGHTS_MIST, FRACTURED_LOVE, CHASING_LIGHTS, ASHES_OF_US, HEATING_SUN, NO_COPYRIGHT_MUSIC, MEDAL, EXCITING_SPORTS_COMPETITIONS, POSITIVE_WAY, ENERGETIC_HAPPY_UPBEAT_ROCK_MUSIC, ENERGY_CATCHER*
        """
)
@RequestMapping("/schedules")
interface ScheduleSwagger {

    /*──────────────────────────────────────────────────────
     * 일정 생성
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "일정 생성",
        description = """
            새로운 스케줄을 생성합니다.
            - `repeatDays` 는 1(일)~7(토) 숫자 배열입니다.
            - `triggeredAt` 은 `HH:mm:ss` 형태의 ISO‑8601 시간 문자열입니다.
            - `isMedicationRequired` 는 복약 여부를 나타내는 boolean 값입니다.
            - `preparationNote` 는 준비물 관련 메모이며 최대 100자까지 입력 가능합니다.
            - `transportType` 은 교통수단 유형입니다. `PUBLIC_TRANSPORT`(대중교통, 기본값) 또는 `CAR`(자가용)
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = ScheduleCreateRequest::class),
                examples = [ExampleObject(
                    name = "createRequest",
                    value = """
                    {
                      "title": "스터디 모임",
                      "isRepeat": true,
                      "repeatDays": [2, 4, 6],
                      "appointmentAt": "2025-05-10T19:00:00",
                      "isMedicationRequired": true,
                      "preparationNote": "준비물: 노트북, 충전기, 약",
                      "transportType": "PUBLIC_TRANSPORT",
                      "departurePlace": {
                        "title": "집",
                        "roadAddress": "서울특별시 강남구 테헤란로 123",
                        "longitude": 127.0276,
                        "latitude": 37.4979
                      },
                      "arrivalPlace": {
                        "title": "카페",
                        "roadAddress": "서울특별시 서초구 서초대로 77",
                        "longitude": 127.0290,
                        "latitude": 37.5010
                      },
                      "preparationAlarm": {
                        "alarmMode": "VIBRATE",
                        "isEnabled": true,
                        "triggeredAt": "2025-05-10T18:30:00",
                        "isSnoozeEnabled": true,
                        "snoozeInterval": 5,
                        "snoozeCount": 3,
                        "soundCategory": "BRIGHT_ENERGY",
                        "ringTone": "FRACTURED_LOVE",
                        "volume": 0.2
                      },
                      "departureAlarm": {
                        "alarmMode": "SOUND",
                        "triggeredAt": "2025-05-10T18:50:00",
                        "isSnoozeEnabled": false,
                        "snoozeInterval": 1,
                        "snoozeCount": -1,
                        "soundCategory": "BRIGHT_ENERGY",
                        "ringTone": "FRACTURED_LOVE",
                        "volume": 0.2
                      }
                    }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                content = [Content(
                    schema = Schema(implementation = ScheduleCreateResponse::class),
                    examples = [ExampleObject(
                        value = """
                                                      {
                                                          "scheduleId": 1001,
                                                          "estimateTime": 15,
                                                          "preparationAlarmTime": "2025-05-10T18:30:00",
                                                          "departureAlarmTime": "2025-05-10T18:50:00",
                                                          "createdAt": "2025-05-10T18:29:30"
                                                      }
                                                    """
                    )]
                )]
            ),
            ApiResponse(responseCode = "400", description = "검증 오류")
        ]
    )
    @PostMapping
    fun createSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: ScheduleCreateRequest,
    ): ScheduleCreateResponse

    /*──────────────────────────────────────────────────────
     * 음성(STT) 기반 빠른 일정 생성
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "빠른 일정 생성",
        description = """
            STT(Speech-to-Text) 음성 인식 결과를 자연어 처리 기반 LLM(GPT)을 통해 파싱된 데이터를 통해 스케줄을 생성합니다.
            알람 관련 시간 계산은 비동기적으로 처리되며, 요청이 성공적으로 수신되면 202 Accepted 상태 코드를 반환합니다.
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = QuickScheduleCreateRequest::class),
                examples = [ExampleObject(
                    name = "voiceCreateRequest",
                    value = """
                    {
                      "appointmentAt": "2025-06-01T14:00:00",
                      "departurePlace": {
                        "title": "회사",
                        "roadAddress": "서울특별시 중구 세종대로 110",
                        "longitude": 126.9784,
                        "latitude": 37.5665
                      },
                      "arrivalPlace": {
                        "title": "식당",
                        "roadAddress": "서울특별시 중구 을지로 50",
                        "longitude": 126.9830,
                        "latitude": 37.5651
                      }
                    }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(responseCode = "202", description = "비동기 처리 시작"),
            ApiResponse(responseCode = "400", description = "검증 오류")
        ]
    )
    @PostMapping("/quick")
    fun createQuickSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: QuickScheduleCreateRequest,
    )

    /*──────────────────────────────────────────────────────
     * STT 일정 텍스트 파싱
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "STT 일정 텍스트 파싱",
        description = """
        한글 자연어 문장(예: "내일 6시에 강남역에 약속 있어")을
        `departurePlaceTitle`, `appointmentAt` JSON 으로 변환합니다.

        **⚠️ Error Code**
        - 잘못된 JSON 형식 `INVALID_JSON`
        - 문장을 이해하지 못하면 `OPEN_AI_PARSING_ERROR`
        - 하루 호출 제한(기본 30회)을 초과하면 `AI_USAGE_LIMIT_EXCEEDED`
        - OpenAI 서버 장애 시 `UNAVAILABLE_OPEN_AI_SERVER`
        - OpenAI 처리 중 예기치 못한 장애 시 `UNHANDLED_OPEN_AI`
        - 그 외 예기치 못한 장애 시 `SERVER_ERROR`
        """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "파싱할 한국어 문장",
            content = [Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ScheduleParsedRequest::class),
                examples = [ExampleObject(
                    name = "예시\u2011요청",
                    value = """
            {
              "text": "내일 6시에 강남역에 약속 있어"
            }"""
                )]
            )]
        ),
        responses = [
            /* ─────────────────────── 200 ─────────────────────── */
            ApiResponse(
                responseCode = "200",
                description = "파싱 성공",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ScheduleParsedResponse::class),
                    examples = [ExampleObject(
                        name = "예시\u2011응답",
                        value = """
                {
                  "departurePlaceTitle": "강남역",
                  "appointmentAt": "2025-04-16T18:00:00"
                }"""
                    )]
                )]
            ),

            /* ─────────────────────── 400 ─────────────────────── */
            ApiResponse(
                responseCode = "400",
                description = "AI가 문장을 이해하지 못했거나, 입력 JSON 형식 오류",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [
                        ExampleObject(
                            name = "형식 오류",
                            value = """
                        {
                          "errorCode": "INVALID_JSON",
                          "message": "잘못된 JSON 형식입니다. 요청 데이터를 확인하세요."
                        }"""
                        ),
                        ExampleObject(
                            name = "파싱 실패",
                            value = """
                        {
                          "errorCode": "OPEN_AI_PARSING_ERROR",
                          "message": "약속 문장을 이해할 수 없습니다. 형식을 확인 후 다시 시도해주세요."
                        }"""
                        )
                    ]
                )]
            ),

            /* ─────────────────────── 429 ─────────────────────── */
            ApiResponse(
                responseCode = "429",
                description = "하루 허용된 AI 사용량(예: 10회) 초과",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        name = "호출 제한 초과",
                        value = """
                {
                  "errorCode": "AI_USAGE_LIMIT_EXCEEDED",
                  "message": "오늘 사용 가능한 AI 사용 횟수를 초과했습니다. MemberId : 42, Date : 2025‑04‑16"
                }"""
                    )]
                )]
            ),

            /* ─────────────────────── 502 ─────────────────────── */
            ApiResponse(
                responseCode = "502",
                description = "OpenAI 서버(Downstream) 장애",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        name = "OpenAI 장애",
                        value = """
                {
                  "errorCode": "UNAVAILABLE_OPEN_AI_SERVER",
                  "message": "일시적으로 Open AI 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
                }"""
                    )]
                )]
            ),

            /* ─────────────────────── 500 ─────────────────────── */
            ApiResponse(
                responseCode = "500",
                description = "OpenAI 처리 과정에서 예기치 못한 오류",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        name = "미처리 예외",
                        value = """
                {
                  "errorCode": "UNHANDLED_OPEN_AI",
                  "message": "Open AI 요청 과정에서 알 수 없는 문제가 발생했습니다. 관리자에게 문의해주세요."
                }"""
                    )]
                )]
            )
        ]
    )
    @PostMapping("/voice")
    fun parseVoiceSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: ScheduleParsedRequest,
    ): ScheduleParsedResponse

    @Operation(
        summary = "[테스트용] 경로에 따른 예상 시간 반환",
        description = """
            **⚠️ 이 엔드포인트는 앱에서 사용하지 않으며, Swagger 테스트 용도입니다.**
            앱에서는 `POST /alarms/setting`을 통해 경로 계산을 수행합니다.

            출발지(startLongitude, startLatitude)와 도착지(endLongitude, endLatitude) 간의
            예상 소요 시간을 분 단위로 계산하여 반환합니다.

            **📌 파라미터 설명**
            - `transportType`: `PUBLIC_TRANSPORT`(대중교통, 기본값) 또는 `CAR`(자가용)
            - `appointmentAt`: 약속 시간 (선택). 자가용(`CAR`) 선택 시 해당 시간대의 예측 교통량을 반영합니다.

            **⚠️ Error Codes**
            - 요청 JSON 문법 오류: `INVALID_JSON`
            - 입력 필드 검증 실패: `FIELD_ERROR`
            - 좌표 형식·범위 오류: `ODSAY_BAD_INPUT`, `ODSAY_MISSING_PARAM`
            - 정류장 없음: `ODSAY_NO_STOP`
            - 서비스 지역 아님: `ODSAY_SERVICE_AREA`
            - 검색 결과 없음: `ODSAY_NO_RESULT`
            - ODsay 서버 내부 오류: `ODSAY_SERVER_ERROR`
            - 예기치 못한 ODsay 오류: `ODSAY_UNHANDLED_ERROR`
            - TMAP 서버 오류: `TMAP_SERVER_ERROR`
            - TMAP 결과 없음: `TMAP_NO_RESULT`
            - 그 외 서버 오류: `SERVER_ERROR`
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "출발·도착 좌표, 교통수단, 약속시간을 담은 요청 바디",
            content = [Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = Schema(implementation = EstimateTimeRequest::class),
                examples = [ExampleObject(
                    name = "예시-요청",
                    value = """
                        {
                          "startLongitude": 127.070593415212,
                          "startLatitude": 37.277975571288,
                          "endLongitude": 126.94569176914,
                          "endLatitude": 37.5959199688468,
                          "transportType": "CAR",
                          "appointmentAt": "2026-03-01T14:00:00"
                        }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "예상 소요 시간 계산 성공",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = EstimateTimeResponse::class),
                    examples = [ExampleObject(
                        name = "예시-응답",
                        value = """
                            {
                              "estimatedTimeInMinutes": 12
                            }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청: JSON 형식 오류, 필드 검증 오류, ODsay 입력값 오류",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [
                        ExampleObject(
                            name = "JSON 형식 오류",
                            value = """
                                {
                                  "errorCode": "INVALID_JSON",
                                  "message": "잘못된 JSON 형식입니다. 요청 데이터를 확인하세요."
                                }"""
                        ),
                        ExampleObject(
                            name = "필드 검증 오류",
                            value = """
                                {
                                  "errorCode": "FIELD_ERROR",
                                  "message": "입력이 잘못되었습니다."
                                }"""
                        ),
                        ExampleObject(
                            name = "ODSay 입력값 범위 오류",
                            value = """
                                {
                                  "errorCode": "ODSAY_BAD_INPUT",
                                  "message": "필수 입력값 형식 및 범위를 확인해주세요: SX"
                                }"""
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "검색 결과 없음",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        name = "검색 결과 없음",
                        value = """
                            {
                              "errorCode": "ODSAY_NO_RESULT",
                              "message": "검색 결과가 없습니다: 출발지→도착지"
                            }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "502",
                description = "ODSay 서버(업스트림) 장애",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        name = "ODSay 서버 오류",
                        value = """
                            {
                              "errorCode": "ODSAY_SERVER_ERROR",
                              "message": "ODSay 서버 내부 오류가 발생했습니다: timeout"
                            }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "ODSay 처리 중 예기치 못한 오류 또는 기타 서버 오류",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [
                        ExampleObject(
                            name = "미처리 ODsay 오류",
                            value = """
                                {
                                  "errorCode": "ODSAY_UNHANDLED_ERROR",
                                  "message": "ODSay API 처리 중 알 수 없는 오류가 발생했습니다: null"
                                }"""
                        ),
                        ExampleObject(
                            name = "서버 오류",
                            value = """
                                {
                                  "errorCode": "SERVER_ERROR",
                                  "message": "서버 오류가 발생했습니다. 관리자에게 문의해주세요."
                                }"""
                        )
                    ]
                )]
            )
        ]
    )
    @PostMapping(value = ["/estimate-time"])
    fun estimateTravelTime(
        @RequestBody request: EstimateTimeRequest,
    ): EstimateTimeResponse

    /*──────────────────────────────────────────────────────
     * 단일 일정 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "단일 일정 상세 조회",
        description = """
            `scheduleId` 로 하나의 스케줄 상세 정보를 반환합니다.
            홈 화면에서 선택한 **일정의 상세 정보(약속, 알람, 장소)** 를 조회할 때 사용합니다.
            """,
        parameters = [
            Parameter(
                name = "scheduleId", `in` = ParameterIn.PATH, required = true,
                description = "조회할 스케줄 ID", example = "1001"
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ScheduleDetailResponse::class),
                    examples = [ExampleObject(value = """
                    {
                      "scheduleId": 1001,
                      "title": "스터디 모임",
                      "isRepeat": true,
                      "repeatDays": [2,4,6],
                      "appointmentAt": "2025-05-10T19:00:00",
                      "preparationAlarm": {
                        "alarmId": 1001,
                        "alarmMode": "VIBRATE",
                        "isEnabled": true,
                        "triggeredAt": "2025-05-10T18:30:00",
                        "isSnoozeEnabled": true,
                        "snoozeInterval": 5,
                        "snoozeCount": 3,
                        "soundCategory": "BRIGHT_ENERGY",
                        "ringTone": "FRACTURED_LOVE",
                        "volume": 0.2
                      },
                      "departureAlarm": {
                        "alarmId": 1002,
                        "alarmMode": "SOUND",
                        "triggeredAt": "2025-05-10T18:50:00",
                        "isSnoozeEnabled": false,
                        "snoozeInterval": 1,
                        "snoozeCount": -1,
                        "soundCategory": "BRIGHT_ENERGY",
                        "ringTone": "FRACTURED_LOVE",
                        "volume": 0.2
                      },
                      "departurePlace": {
                        "title": "집",
                        "roadAddress": "서울특별시 강남구 테헤란로 123",
                        "longitude": 127.0276,
                        "latitude": 37.4979
                      },
                      "arrivalPlace": {
                        "title": "카페",
                        "roadAddress": "서울특별시 서초구 서초대로 77",
                        "longitude": 127.0290,
                        "latitude": 37.5010
                      },
                      "transportType": "PUBLIC_TRANSPORT"
                    }""")]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "스케줄 또는 멤버가 존재하지 않습니다.",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_FOUND_SCHEDULE",
                            summary = "스케줄 없음",
                            value = """
                            {
                              "errorCode": "NOT_FOUND_SCHEDULE",
                              "message": "일정을 찾을 수 없습니다. ScheduleId : 1001"
                            }
                            """
                        ),
                        ExampleObject(
                            name = "NOT_FOUND_MEMBER",
                            summary = "멤버 없음",
                            value = """
                            {
                              "errorCode": "NOT_FOUND_MEMBER",
                              "message": "회원을 찾을 수 없습니다. MemberId : 123"
                            }
                            """
                        )
                    ]
                )]
            )
        ]
    )
    @GetMapping("/{scheduleId}")
    fun getSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
    ): ScheduleDetailResponse

    /*──────────────────────────────────────────────────────
     * 일정 준비 정보 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "일정 준비 정보 조회",
        description = """
        `scheduleId`에 해당하는 일정의 **준비 정보**를 반환합니다.
        - `isMedicationRequired`: 복약 여부
        - `preparationNote`: 준비물 메모 (최대 100자)
        """,
        parameters = [
            Parameter(
                name = "scheduleId",
                `in` = ParameterIn.PATH,
                required = true,
                description = "조회할 스케줄 ID",
                example = "1001"
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "준비 정보 조회 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = SchedulePreparationResponse::class),
                    examples = [ExampleObject(value = """
                    {
                      "isMedicationRequired": true,
                      "preparationNote": "노트북, 충전기, 처방약 준비"
                    }
                """)]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "스케줄을 찾을 수 없음",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "NOT_FOUND_SCHEDULE",
                        summary = "스케줄 없음",
                        value = """
                        {
                          "errorCode": "NOT_FOUND_SCHEDULE",
                          "message": "일정을 찾을 수 없습니다. ScheduleId : 1001"
                        }
                    """
                    )]
                )]
            )
        ]
    )
    fun getPreparationInfo(
        @PathVariable scheduleId: Long,
    ): SchedulePreparationResponse

    /*──────────────────────────────────────────────────────
     * 일정별 이슈 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "일정별 이슈 조회",
        description = """
            `scheduleId`에 해당하는 일정의 **도착지** 주변 긴급 재난문자 및 지하철 알림 메시지를 한데 모아
            각 메시지를 줄바꿈(`\n`)으로 구분한 단일 문자열로 반환합니다.
            사용자가 설정한 일정의 도착지에 등록된 이슈를 빠르게 확인할 때 사용합니다.
            """,
        parameters = [
            Parameter(
                name = "scheduleId",
                `in` = ParameterIn.PATH,
                required = true,
                description = "조회할 스케줄 ID",
                example = "1001"
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "이슈 조회 성공",
                content = [Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = Schema(type = "string"),
                    examples = [ExampleObject(value = """
                        목적지 인근에 다음과 같은 이슈가 있습니다.
                        오늘 06:09:03: 3호선 특정장애인단체 시위 예정으로 지연될 수 있습니다.
                        오늘 03:07:04: 서울특별시 동작구에 호우경보가 발령되었습니다.
                        """)]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "스케줄 또는 멤버를 찾을 수 없음",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_FOUND_SCHEDULE",
                            summary = "스케줄 없음",
                            value = """
                            {
                              "errorCode": "NOT_FOUND_SCHEDULE",
                              "message": "일정을 찾을 수 없습니다. ScheduleId : 1001"
                            }
                            """
                        )
                    ]
                )]
            )
        ]
    )
    fun getScheduleIssues(
        @PathVariable scheduleId: Long,
    ): String

    /*──────────────────────────────────────────────────────
     * 홈 화면 일정 목록 조회 (무한 스크롤)
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "전체 일정 목록 조회",
        description = """
            **가장 빨리 울릴 알람 시각**(earliestAlarmAt)과 **일정 목록**(scheduleList)를 반환합니다.
            - `hasNext=true` 면 이후 page 를 조회할 수 있습니다.
            - `page`와 `size`를 통해 페이지와 개수를 조정할 수 있습니다.
            """,
        parameters = [
            Parameter(
                name = "page", `in` = ParameterIn.QUERY,
                description = "0부터 시작하는 페이지 번호", example = "0"
            ),
            Parameter(
                name = "size", `in` = ParameterIn.QUERY,
                description = "페이지당 항목 수", example = "20"
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = HomeScheduleListResponse::class),
                    examples = [ExampleObject(value = """
                    {
                      "earliestAlarmAt": "2025-05-10T18:30:00",
                      "hasNext": false,
                      "scheduleList": [
                        {
                          "scheduleId": 1001,
                          "startLongitude": 127.0276,
                          "startLatitude": 37.4979,
                          "endLongitude": 127.029,
                          "endLatitude": 37.501,
                          "scheduleTitle": "스터디 모임",
                          "isRepeat": true,
                          "repeatDays": [2,4,6],
                          "appointmentAt": "2025-05-10T19:00:00",
                          "preparationAlarm": {
                            "alarmId": 1001,
                            "alarmMode": "VIBRATE",
                            "isEnabled": true,
                            "triggeredAt": "2025-05-10T18:30:00",
                            "isSnoozeEnabled": true,
                            "snoozeInterval": 5,
                            "snoozeCount": 3,
                            "soundCategory": "BRIGHT_ENERGY",
                            "ringTone": "FRACTURED_LOVE",
                            "volume": 0.2
                          },
                          "departureAlarm": {
                            "alarmId": 1002,
                            "alarmMode": "SOUND",
                            "isEnabled": true,
                            "triggeredAt": "2025-05-10T18:50:00",
                            "isSnoozeEnabled": false,
                            "snoozeInterval": 1,
                            "snoozeCount": -1,
                            "soundCategory": "BRIGHT_ENERGY",
                            "ringTone": "FRACTURED_LOVE",
                            "volume": 0.2
                          },
                          "hasActiveAlarm": true,
                          "preparationNote": "우산 챙기기"
                        }
                      ]
                    }
                    """)]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "멤버 없음",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(value = """
                    {
                      "errorCode": "NOT_FOUND_MEMBER",
                      "message": "회원을 찾을 수 없습니다. MemberId : 123"
                    }""")]
                )]
            )
        ]
    )
    @GetMapping
    fun getActiveSchedules(
        @RequestAttribute("memberId") memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): HomeScheduleListResponse

    /*──────────────────────────────────────────────────────
     * 일정 수정
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "일정 수정",
        description = """
        스케줄을 수정합니다.
        - 출발/도착 장소 또는 약속 시간이 바뀌면 202 Accepted
        - 그 외 필드만 바뀌면 200 OK
        """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = ScheduleUpdateRequest::class),
                examples = [ExampleObject(
                    name = "updateRequest",
                    value = """
                    {
                      "title": "스터디 모임(수정)",
                      "isRepeat": true,
                      "repeatDays": [2, 4, 6],
                      "appointmentAt": "2025-05-10T20:00:00",
                      "departurePlace": {
                        "title": "집",
                        "roadAddress": "서울특별시 강남구 테헤란로 123",
                        "longitude": 127.0276,
                        "latitude": 37.4979
                      },
                      "arrivalPlace": {
                        "title": "새 카페",
                        "roadAddress": "서울특별시 강남구 강남대로 321",
                        "longitude": 127.0310,
                        "latitude": 37.4988
                      },
                      "preparationAlarm": {
                        "alarmMode": "VIBRATE",
                        "isEnabled": true,
                        "triggeredAt": "2025-05-10T18:45:00",
                        "isSnoozeEnabled": true,
                        "snoozeInterval": 5,
                        "snoozeCount": 3,
                        "soundCategory": "BRIGHT_ENERGY",
                        "ringTone": "FRACTURED_LOVE",
                        "volume": 0.2
                      },
                      "departureAlarm": {
                        "alarmMode": "SOUND",
                        "triggeredAt": "2025-05-10T19:10:00",
                        "isSnoozeEnabled": false,
                        "snoozeInterval": 1,
                        "snoozeCount": -1,
                        "soundCategory": "BRIGHT_ENERGY",
                        "ringTone": "FRACTURED_LOVE",
                        "volume": 0.2
                      },
                      "isMedicationRequired": false,
                      "preparationNote": "노트북, 충전기 챙기기"
                    }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공 (재계산 불필요)",
                content = [Content(
                    schema = Schema(implementation = ScheduleUpdateResponse::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "scheduleId": 1001,
                          "updatedAt": "2025-05-10T18:40:00"
                        }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "202",
                description = "수정 성공, 경로 재계산 필요",
                content = [Content(schema = Schema(implementation = ScheduleUpdateResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "스케줄 또는 멤버 없음")
        ]
    )
    @PutMapping("/{scheduleId}")
    fun updateSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody request: ScheduleUpdateRequest,
    ): ResponseEntity<ScheduleUpdateResponse>

    @Operation(
        summary = "알람 ON/OFF",
        description = "**일정의 알람 동작 여부**를 isEnabled 의 true/false 로 변경합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = AlarmSwitchRequest::class),
                examples = [ExampleObject(value = """
            { "isEnabled": false }
            """)]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "변경 완료",
                content = [Content(
                    schema = Schema(
                        implementation = AlarmSwitchResponse::class
                    )
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "멤버 또는 스케줄 없음",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "ScheduleNotFound",
                            value = """
                    { "errorCode":"NOT_FOUND_SCHEDULE",
                      "message":"일정을 찾을 수 없습니다. ScheduleId : 1001" }"""
                        ),
                        ExampleObject(
                            name = "MemberNotFound",
                            value = """
                    { "errorCode":"NOT_FOUND_MEMBER",
                      "message":"회원을 찾을 수 없습니다. MemberId : 123" }"""
                        )
                    ]
                )]
            )
        ]
    )
    @PatchMapping("/{scheduleId}/alarm")
    fun switchAlarm(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody request: AlarmSwitchRequest,
    ): AlarmSwitchResponse

    /*──────────────────────────────────────────────────────
     * 에브리타임 URL 검증 및 시간표 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "에브리타임 URL 검증 및 시간표 조회",
        description = """
            에브리타임 공유 URL의 유효성을 검증하고, 요일별 시간표를 반환합니다.
            - URL 형식 검증 (everytime.kr 도메인, /@{identifier} 경로)
            - 실제 에브리타임 API 호출을 통한 시간표 조회
            - 응답의 `timetable`은 요일별(MONDAY~SUNDAY) 수업 목록이며, 월요일부터 시작하여 시간순으로 정렬됩니다
            - 수업이 없는 요일은 응답에서 제외됩니다

            **⚠️ Error Codes**
            - URL 형식 오류: `EVERYTIME_INVALID_URL`
            - 시간표를 찾을 수 없음 (비공개/삭제): `EVERYTIME_NOT_FOUND`
            - 수업이 없는 시간표: `EVERYTIME_EMPTY_TIMETABLE`
            - 에브리타임 서버 오류: `EVERYTIME_SERVER_ERROR`
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = Schema(implementation = EverytimeValidateRequest::class),
                examples = [ExampleObject(
                    name = "예시-요청",
                    value = """
                    {
                      "everytimeUrl": "https://everytime.kr/@ip9ktZ3A7H35H6P7Z1Wr"
                    }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "검증 성공 및 시간표 반환",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = EverytimeValidateResponse::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "timetable": {
                            "MONDAY": [
                              { "courseName": "드론과 로보틱스", "startTime": "10:00:00", "endTime": "12:00:00" },
                              { "courseName": "시스템아키텍처", "startTime": "12:00:00", "endTime": "14:00:00" },
                              { "courseName": "P-실무프로젝트", "startTime": "18:00:00", "endTime": "21:00:00" }
                            ],
                            "TUESDAY": [
                              { "courseName": "컴퓨터구조", "startTime": "11:00:00", "endTime": "13:00:00" },
                              { "courseName": "시스템아키텍처", "startTime": "14:00:00", "endTime": "16:00:00" },
                              { "courseName": "기업과 리더십", "startTime": "16:00:00", "endTime": "18:00:00" },
                              { "courseName": "P-실무프로젝트", "startTime": "18:00:00", "endTime": "21:00:00" }
                            ],
                            "WEDNESDAY": [
                              { "courseName": "ㅎㅎ", "startTime": "12:00:00", "endTime": "13:00:00" },
                              { "courseName": "P-실무프로젝트", "startTime": "18:00:00", "endTime": "21:00:00" }
                            ],
                            "THURSDAY": [
                              { "courseName": "취/창업 진로세미나", "startTime": "09:00:00", "endTime": "10:00:00" },
                              { "courseName": "드론과 로보틱스", "startTime": "10:00:00", "endTime": "12:00:00" },
                              { "courseName": "컴퓨터구조", "startTime": "11:00:00", "endTime": "13:00:00" },
                              { "courseName": "드론과 로보틱스", "startTime": "14:00:00", "endTime": "16:00:00" },
                              { "courseName": "P-실무프로젝트", "startTime": "18:00:00", "endTime": "21:00:00" }
                            ],
                            "FRIDAY": [
                              { "courseName": "P-실무프로젝트", "startTime": "18:00:00", "endTime": "21:00:00" }
                            ]
                          }
                        }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "URL 형식 오류",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        value = """
                        {
                          "errorCode": "EVERYTIME_INVALID_URL",
                          "message": "에브리타임 URL 형식이 올바르지 않습니다: https://example.com/test"
                        }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "시간표를 찾을 수 없음",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [
                        ExampleObject(
                            name = "시간표 없음",
                            value = """
                            {
                              "errorCode": "EVERYTIME_NOT_FOUND",
                              "message": "에브리타임 시간표를 찾을 수 없습니다. 공유 URL을 확인해주세요."
                            }"""
                        ),
                        ExampleObject(
                            name = "빈 시간표",
                            value = """
                            {
                              "errorCode": "EVERYTIME_EMPTY_TIMETABLE",
                              "message": "시간표에 등록된 수업이 없습니다."
                            }"""
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "502",
                description = "에브리타임 서버 장애",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        value = """
                        {
                          "errorCode": "EVERYTIME_SERVER_ERROR",
                          "message": "에브리타임 서버에 일시적인 오류가 발생했습니다: 500 INTERNAL_SERVER_ERROR"
                        }"""
                    )]
                )]
            )
        ]
    )
    @PostMapping("/everytime/validate")
    fun validateEverytimeUrl(
        @RequestBody request: EverytimeValidateRequest,
    ): EverytimeValidateResponse

    /*──────────────────────────────────────────────────────
     * 에브리타임 시간표 기반 스케줄 일괄 생성
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "에브리타임 시간표 기반 스케줄 일괄 생성",
        description = """
            유저가 선택한 요일별 수업을 기반으로 반복 스케줄을 일괄 생성합니다.
            먼저 `/schedules/everytime/validate`를 호출하여 시간표를 조회한 뒤,
            유저가 요일별로 하나씩 선택한 수업 정보를 `selectedLectures`로 전달합니다.

            **📌 생성 규칙**
            - `selectedLectures`에 요일별 최대 1개의 수업을 선택합니다 (없는 요일은 생략)
            - 동일한 시작시간의 요일들은 하나의 반복 스케줄로 묶입니다
              (예: 월/수 09:30 선택 → "월/수요일 학교")
            - 각 스케줄에는 멤버 기본 알람 설정이 적용됩니다
            - `transportType` 미지정 시 `PUBLIC_TRANSPORT`(대중교통)로 처리

            **🚗 경로 계산**
            - 대중교통: 1회 조회 후 전체 그룹에 재사용
            - 자가용: 시간대별 조회

            **⚠️ Error Codes**
            - 경로 계산 오류: `ODSAY_*`, `TMAP_*` 계열 에러 코드
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = Schema(implementation = EverytimeScheduleCreateRequest::class),
                examples = [ExampleObject(
                    name = "예시-요청",
                    value = """
                    {
                      "selectedLectures": [
                        { "day": "MONDAY", "startTime": "10:00:00" },
                        { "day": "TUESDAY", "startTime": "11:00:00" },
                        { "day": "THURSDAY", "startTime": "09:00:00" }
                      ],
                      "departurePlace": {
                        "title": "집",
                        "roadAddress": "서울특별시 강남구 테헤란로 123",
                        "longitude": 127.070593415212,
                        "latitude": 37.277975571288
                      },
                      "arrivalPlace": {
                        "title": "학교",
                        "roadAddress": "서울특별시 서초구 서초대로 77",
                        "longitude": 126.94569176914,
                        "latitude": 37.5959199688468
                      },
                      "transportType": "PUBLIC_TRANSPORT"
                    }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "스케줄 일괄 생성 성공",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = EverytimeScheduleCreateResponse::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "createdCount": 2,
                          "schedules": [
                            {
                              "scheduleId": 101,
                              "title": "목요일 학교",
                              "repeatDays": [5],
                              "appointmentAt": "2026-03-12T09:00:00"
                            },
                            {
                              "scheduleId": 102,
                              "title": "월요일 학교",
                              "repeatDays": [2],
                              "appointmentAt": "2026-03-09T10:00:00"
                            },
                            {
                              "scheduleId": 103,
                              "title": "화요일 학교",
                              "repeatDays": [3],
                              "appointmentAt": "2026-03-10T11:00:00"
                            }
                          ]
                        }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "검증 오류",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        value = """
                        {
                          "errorCode": "BAD_REQUEST",
                          "message": "selectedLectures는 필수입니다."
                        }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "멤버 없음",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        value = """
                        {
                          "errorCode": "NOT_FOUND_MEMBER",
                          "message": "회원을 찾을 수 없습니다. MemberId : 123"
                        }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "502",
                description = "경로 API 서버 장애",
                content = [Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        value = """
                        {
                          "errorCode": "ODSAY_SERVER_ERROR",
                          "message": "ODsay 서버에 일시적인 오류가 발생했습니다."
                        }"""
                    )]
                )]
            )
        ]
    )
    @PostMapping("/everytime")
    fun createSchedulesFromEverytime(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: EverytimeScheduleCreateRequest,
    ): EverytimeScheduleCreateResponse

    /*──────────────────────────────────────────────────────
     * 일정 삭제
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "일정 삭제",
        description = "스케줄을 삭제합니다.",
        responses = [
            ApiResponse(responseCode = "204", description = "삭제 완료"),
            ApiResponse(responseCode = "404", description = "스케줄 또는 멤버 없음")
        ]
    )
    @DeleteMapping("/{scheduleId}")
    fun deleteSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
    )
}
