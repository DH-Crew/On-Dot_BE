package com.dh.ondot.schedule.api.swagger;

import com.dh.ondot.core.domain.ErrorResponse;
import com.dh.ondot.schedule.api.request.*;
import com.dh.ondot.schedule.api.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(
        name = "Schedule API",
        description = """
        <b>AccessToken (Authorization: Bearer JWT)</b>은 필수값입니다.<br>
        → 각 엔드포인트에 별도 파라미터는 필요 없으나 오른쪽 상단 Authorize에 토큰 값을 넣어야합니다.<br><br>

        <b>🗓 repeatDays 요일 규칙</b><br>
        • 1 = 일요일<br>
        • 2 = 월요일 …<br>
        • 7 = 토요일<br><br>

        <b>🔔 Alarm ENUM</b><br>
        • <code>AlarmMode</code>: SILENT, VIBRATE, SOUND<br>
        • <code>SnoozeInterval</code>: 1, 3, 5, 10, 30, 60 (분)<br>
        • <code>SnoozeCount</code>: -1(INFINITE), 1, 3, 5, 10 (회)<br>
        • <code>SoundCategory</code>: <i>BRIGHT_ENERGY, FAST_INTENSE</i><br>
        • <code>RingTone</code>: <i>
          DANCING_IN_THE_STARDUST, IN_THE_CITY_LIGHTS_MIST, FRACTURED_LOVE,<br>
          CHASING_LIGHTS, ASHES_OF_US, HEATING_SUN, NO_COPYRIGHT_MUSIC,<br>
          MEDAL, EXCITING_SPORTS_COMPETITIONS, POSITIVE_WAY,<br>
          ENERGETIC_HAPPY_UPBEAT_ROCK_MUSIC, ENERGY_CATCHER
        </i>
        """
)
@RequestMapping("/schedules")
public interface ScheduleSwagger {

    /*──────────────────────────────────────────────────────
     * 일정 생성
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "일정 생성",
            description = """
            새로운 스케줄을 생성합니다. <br>
            <ul>
              <li><code>repeatDays</code> 는 1(일)~7(토) 숫자 배열입니다.</li>
              <li><code>triggeredAt</code> 은 <code>HH:mm:ss</code> 형태의 ISO‑8601 시간 문자열입니다.</li>
            </ul>""",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ScheduleCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "createRequest",
                                    value = """
                    {
                      "title": "스터디 모임",
                      "isRepeat": true,
                      "repeatDays": [2, 4, 6],
                      "appointmentAt": "2025-05-10T19:00:00",
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
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "생성 성공",
                            content = @Content(schema = @Schema(implementation = ScheduleCreateResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "scheduleId": 1001,
                          "createdAt": "2025-05-10T18:29:30"
                        }"""
                                    ))),
                    @ApiResponse(responseCode = "400", description = "검증 오류")
            }
    )
    @PostMapping
    ScheduleCreateResponse createSchedule(
            @RequestAttribute("memberId") Long memberId,
            @RequestBody ScheduleCreateRequest request
    );

    /*──────────────────────────────────────────────────────
     * 음성(STT) 기반 빠른 일정 생성
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "빠른 일정 생성",
            description = """
            STT(Speech-to-Text) 음성 인식 결과를 자연어 처리 기반 LLM(GPT)을 통해 파싱된 데이터를 통해 스케줄을 생성합니다.<br>
            알람 관련 시간 계산은 비동기적으로 처리되며, 요청이 성공적으로 수신되면 202 Accepted 상태 코드를 반환합니다.
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = QuickScheduleCreateRequest.class),
                            examples = @ExampleObject(
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
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "비동기 처리 시작"),
                    @ApiResponse(responseCode = "400", description = "검증 오류")
            }
    )
    @PostMapping("/quick")
    void createQuickSchedule(
            @RequestAttribute("memberId") Long memberId,
            @RequestBody QuickScheduleCreateRequest request
    );

    /*──────────────────────────────────────────────────────
     * STT 일정 텍스트 파싱
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "STT 일정 텍스트 파싱",
            description = """
        한글 자연어 문장(예: “내일 6시에 강남역에 약속 있어”)을
        <code>departurePlaceTitle</code>, <code>appointmentAt</code> JSON 으로 변환합니다.
        <br/><br/>
        <b>⚠️ Error Code </b><br/>
        - 잘못된 JSON 형식 <code>INVALID_JSON</code><br/>
        - 문장을 이해하지 못하면 <code>OPEN_AI_PARSING_ERROR</code><br/>
        - 하루 호출 제한(기본 30회)을 초과하면 <code>AI_USAGE_LIMIT_EXCEEDED</code><br/>
        - OpenAI 서버 장애 시 <code>UNAVAILABLE_OPEN_AI_SERVER</code><br/>
        - OpenAI 처리 중 예기치 못한 장애 시 <code>UNHANDLED_OPEN_AI</code><br/>
        - 그 외 예기치 못한 장애 시 <code>SERVER_ERROR</code>
        """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "파싱할 한국어 문장",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema    = @Schema(implementation = ScheduleParsedRequest.class),
                            examples  = @ExampleObject(name = "예시‑요청", value = """
            {
              "text": "내일 6시에 강남역에 약속 있어"
            }""")
                    )
            ),
            responses = {
                    /* ─────────────────────── 200 ─────────────────────── */
                    @ApiResponse(
                            responseCode = "200",
                            description  = "파싱 성공",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(implementation = ScheduleParsedResponse.class),
                                    examples  = @ExampleObject(name="예시‑응답", value = """
                {
                  "departurePlaceTitle": "강남역",
                  "appointmentAt": "2025-04-16T18:00:00"
                }""")
                            )
                    ),

                    /* ─────────────────────── 400 ─────────────────────── */
                    @ApiResponse(
                            responseCode = "400",
                            description  = "AI가 문장을 이해하지 못했거나, 입력 JSON 형식 오류",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(ref = "#/components/schemas/ErrorResponse"),
                                    examples  = {
                                            @ExampleObject(name="형식 오류",
                                                    value = """
                        {
                          "errorCode": "INVALID_JSON",
                          "message":   "잘못된 JSON 형식입니다. 요청 데이터를 확인하세요."
                        }"""),
                                            @ExampleObject(name="파싱 실패",
                                                    value = """
                        {
                          "errorCode": "OPEN_AI_PARSING_ERROR",
                          "message":   "약속 문장을 이해할 수 없습니다. 형식을 확인 후 다시 시도해주세요."
                        }""")
                                    }
                            )
                    ),

                    /* ─────────────────────── 429 ─────────────────────── */
                    @ApiResponse(
                            responseCode = "429",
                            description  = "하루 허용된 AI 사용량(예: 10회) 초과",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(ref = "#/components/schemas/ErrorResponse"),
                                    examples  = @ExampleObject(name="호출 제한 초과", value = """
                {
                  "errorCode": "AI_USAGE_LIMIT_EXCEEDED",
                  "message":   "오늘 사용 가능한 AI 사용 횟수를 초과했습니다. MemberId : 42, Date : 2025‑04‑16"
                }""")
                            )
                    ),

                    /* ─────────────────────── 502 ─────────────────────── */
                    @ApiResponse(
                            responseCode = "502",
                            description  = "OpenAI 서버(Downstream) 장애",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(ref = "#/components/schemas/ErrorResponse"),
                                    examples  = @ExampleObject(name="OpenAI 장애", value = """
                {
                  "errorCode": "UNAVAILABLE_OPEN_AI_SERVER",
                  "message":   "일시적으로 Open AI 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
                }""")
                            )
                    ),

                    /* ─────────────────────── 500 ─────────────────────── */
                    @ApiResponse(
                            responseCode = "500",
                            description  = "OpenAI 처리 과정에서 예기치 못한 오류",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(ref = "#/components/schemas/ErrorResponse"),
                                    examples  = @ExampleObject(name="미처리 예외", value = """
                {
                  "errorCode": "UNHANDLED_OPEN_AI",
                  "message":   "Open AI 요청 과정에서 알 수 없는 문제가 발생했습니다. 관리자에게 문의해주세요."
                }""")
                            )
                    )
            }
    )
    @PostMapping("/nlp")
    ScheduleParsedResponse parse(
            @RequestAttribute("memberId") Long memberId,
            @RequestBody ScheduleParsedRequest request
    );

    @Operation(
            summary = "경로에 따른 예상 시간 반환",
            description = """
            출발지(startLongitude, startLatitude)와 도착지(endLongitude, endLatitude) 간의
            대중교통 예상 소요 시간을 분 단위로 계산하여 반환합니다.
            <br/><br/>
            <b>⚠️ Error Codes</b><br/>
            • 요청 JSON 문법 오류: <code>INVALID_JSON</code><br/>
            • 입력 필드 검증 실패: <code>FIELD_ERROR</code><br/>
            • 좌표 형식·범위 오류: <code>ODSAY_BAD_INPUT</code>, <code>ODSAY_MISSING_PARAM</code><br/>
            • 정류장 없음: <code>ODSAY_NO_STOP</code><br/>
            • 서비스 지역 아님: <code>ODSAY_SERVICE_AREA</code><br/>
            • 검색 결과 없음: <code>ODSAY_NO_RESULT</code><br/>
            • ODsay 서버 내부 오류: <code>ODSAY_SERVER_ERROR</code><br/>
            • 예기치 못한 ODsay 오류: <code>ODSAY_UNHANDLED_ERROR</code><br/>
            • 그 외 서버 오류: <code>SERVER_ERROR</code>
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required    = true,
                    description = "출발·도착 좌표를 담은 요청 바디",
                    content     = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema    = @Schema(implementation = EstimateTimeRequest.class),
                            examples  = @ExampleObject(
                                    name  = "예시-요청",
                                    value = """
                        {
                          "startLongitude": 127.070593415212,
                          "startLatitude": 37.277975571288,
                          "endLongitude": 126.94569176914,
                          "endLatitude": 37.5959199688468
                        }"""
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "예상 소요 시간 계산 성공",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(implementation = EstimateTimeResponse.class),
                                    examples  = @ExampleObject(
                                            name  = "예시-응답",
                                            value = """
                            {
                              "estimatedTimeInMinutes": 12
                            }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description  = "잘못된 요청: JSON 형식 오류, 필드 검증 오류, ODsay 입력값 오류",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(ref = "#/components/schemas/ErrorResponse"),
                                    examples  = {
                                            @ExampleObject(
                                                    name  = "JSON 형식 오류",
                                                    value = """
                                {
                                  "errorCode": "INVALID_JSON",
                                  "message":   "잘못된 JSON 형식입니다. 요청 데이터를 확인하세요."
                                }"""
                                            ),
                                            @ExampleObject(
                                                    name  = "필드 검증 오류",
                                                    value = """
                                {
                                  "errorCode": "FIELD_ERROR",
                                  "message":   "입력이 잘못되었습니다."
                                }"""
                                            ),
                                            @ExampleObject(
                                                    name  = "ODSay 입력값 범위 오류",
                                                    value = """
                                {
                                  "errorCode": "ODSAY_BAD_INPUT",
                                  "message":   "필수 입력값 형식 및 범위를 확인해주세요: SX"
                                }"""
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description  = "검색 결과 없음",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(ref = "#/components/schemas/ErrorResponse"),
                                    examples  = @ExampleObject(
                                            name  = "검색 결과 없음",
                                            value = """
                            {
                              "errorCode": "ODSAY_NO_RESULT",
                              "message":   "검색 결과가 없습니다: 출발지→도착지"
                            }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description  = "ODSay 서버(업스트림) 장애",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(ref = "#/components/schemas/ErrorResponse"),
                                    examples  = @ExampleObject(
                                            name  = "ODSay 서버 오류",
                                            value = """
                            {
                              "errorCode": "ODSAY_SERVER_ERROR",
                              "message":   "ODSay 서버 내부 오류가 발생했습니다: timeout"
                            }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description  = "ODSay 처리 중 예기치 못한 오류 또는 기타 서버 오류",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(ref = "#/components/schemas/ErrorResponse"),
                                    examples  = {
                                            @ExampleObject(
                                                    name  = "미처리 ODsay 오류",
                                                    value = """
                                {
                                  "errorCode": "ODSAY_UNHANDLED_ERROR",
                                  "message":   "ODSay API 처리 중 알 수 없는 오류가 발생했습니다: null"
                                }"""
                                            ),
                                            @ExampleObject(
                                                    name  = "서버 오류",
                                                    value = """
                                {
                                  "errorCode": "SERVER_ERROR",
                                  "message":   "서버 오류가 발생했습니다. 관리자에게 문의해주세요."
                                }"""
                                            )
                                    }
                            )
                    )
            }
    )
    @PostMapping(value = "/estimate-time")
    EstimateTimeResponse estimateTravelTime(
            @RequestBody EstimateTimeRequest request
    );

    /*──────────────────────────────────────────────────────
     * 단일 일정 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary     = "단일 일정 상세 조회",
            description = """
            <code>scheduleId</code> 로 하나의 스케줄 상세 정보를 반환합니다.<br>
            홈 화면에서 선택한 <b>일정의 상세 정보(약속, 알람, 장소)</b>를 조회할 때 사용합니다.
            """,
            parameters  = {
                    @Parameter(name="scheduleId", in=ParameterIn.PATH, required=true,
                            description="조회할 스케줄 ID", example="1001")
            },
            responses   = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(implementation = ScheduleDetailResponse.class),
                                    examples  = @ExampleObject(value = """
                    {
                      "title": "스터디 모임",
                      "isRepeat": true,
                      "repeatDays": [2,4,6],
                      "appointmentAt": "2025-05-10T19:00:00",
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
                      }
                    }""")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description  = "스케줄 또는 멤버가 존재하지 않습니다.",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(implementation = ErrorResponse.class),
                                    examples  = {
                                            @ExampleObject(
                                                    name   = "NOT_FOUND_SCHEDULE",
                                                    summary= "스케줄 없음",
                                                    value  = """
                            {
                              "errorCode": "NOT_FOUND_SCHEDULE",
                              "message": "일정을 찾을 수 없습니다. ScheduleId : 1001"
                            }
                            """
                                            ),
                                            @ExampleObject(
                                                    name   = "NOT_FOUND_MEMBER",
                                                    summary= "멤버 없음",
                                                    value  = """
                            {
                              "errorCode": "NOT_FOUND_MEMBER",
                              "message": "회원을 찾을 수 없습니다. MemberId : 123"
                            }
                            """
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/{scheduleId}")
    ScheduleDetailResponse getSchedule(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long scheduleId
    );

    /*──────────────────────────────────────────────────────
     * 홈 화면 일정 목록 조회 (무한 스크롤)
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary     = "전체 일정 목록 조회",
            description = """
            <b>가장 빨리 울릴 알람 시각</b>(nextAlarmAt)과 <b>일정 목록</b>(scheduleList)를 반환합니다. <br>
            • <code>hasNext=true</code> 면 이후 page 를 조회할 수 있습니다. <br>
            • <code>page</code>와 <code>size</code>를 통해 페이지와 개수를 조정할 수 있습니다.
            """,
            parameters  = {
                    @Parameter(name="page", in= ParameterIn.QUERY,
                            description="0부터 시작하는 페이지 번호", example="0"),
                    @Parameter(name="size", in=ParameterIn.QUERY,
                            description="페이지당 항목 수", example="20")
            },
            responses   = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "조회 성공",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(implementation = HomeScheduleListResponse.class),
                                    examples  = @ExampleObject(value = """
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
                            "triggeredAt": "2025-05-10T18:50:00",
                            "isSnoozeEnabled": false,
                            "snoozeInterval": 1,
                            "snoozeCount": -1,
                            "soundCategory": "BRIGHT_ENERGY",
                            "ringTone": "FRACTURED_LOVE",
                            "volume": 0.2
                          },
                          "nextAlarmAt": "2025-05-10T18:30:00",
                          "isEnabled": true
                        }
                      ]
                    }
                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description  = "멤버 없음",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(implementation = ErrorResponse.class),
                                    examples  = @ExampleObject(value = """
                    {
                      "errorCode": "NOT_FOUND_MEMBER",
                      "message": "회원을 찾을 수 없습니다. MemberId : 123"
                    }""")
                            )
                    )
            }
    )
    @GetMapping
    HomeScheduleListResponse getSchedules(
            @RequestAttribute("memberId") Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    /*──────────────────────────────────────────────────────
     * 일정 수정
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "일정 수정",
            description = """
        스케줄을 수정합니다.  
        <ul>
          <li>출발/도착 장소 또는 약속 시간이 바뀌면 202 Accepted</li>
          <li>그 외 필드만 바뀌면 200 OK</li>
        </ul>""",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ScheduleUpdateRequest.class),
                            examples = @ExampleObject(
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
                      }
                    }"""
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "수정 성공 (재계산 불필요)",
                            content = @Content(schema = @Schema(implementation = ScheduleUpdateResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "scheduleId": 1001,
                          "updatedAt": "2025-05-10T18:40:00"
                        }"""
                                    ))),
                    @ApiResponse(responseCode = "202",
                            description = "수정 성공, 경로 재계산 필요",
                            content = @Content(schema = @Schema(implementation = ScheduleUpdateResponse.class))),
                    @ApiResponse(responseCode = "404", description = "스케줄 또는 멤버 없음")
            }
    )
    @PutMapping("/{scheduleId}")
    ResponseEntity<ScheduleUpdateResponse> updateSchedule(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateRequest request
    );

    @Operation(
            summary = "알람 ON/OFF",
            description = "<b>일정의 알람 동작 여부</b>를 isEnabled 의 true/false 로 변경합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema   = @Schema(implementation = AlarmSwitchRequest.class),
                            examples = @ExampleObject(value = """
            { "isEnabled": false }
            """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "변경 완료",
                            content      = @Content(schema = @Schema(
                                    implementation = AlarmSwitchResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description  = "멤버 또는 스케줄 없음",
                            content      = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema    = @Schema(implementation = ErrorResponse.class),
                                    examples  = {
                                            @ExampleObject(name="ScheduleNotFound", value="""
                    { "errorCode":"NOT_FOUND_SCHEDULE",
                      "message":"일정을 찾을 수 없습니다. ScheduleId : 1001" }"""),
                                            @ExampleObject(name="MemberNotFound", value="""
                    { "errorCode":"NOT_FOUND_MEMBER",
                      "message":"회원을 찾을 수 없습니다. MemberId : 123" }""")
                                    }
                            )
                    )
            }
    )
    @PatchMapping("/{scheduleId}/alarm")
    AlarmSwitchResponse switchAlarm(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long scheduleId,
            @RequestBody AlarmSwitchRequest request
    );

    /*──────────────────────────────────────────────────────
     * 일정 삭제
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "일정 삭제",
            description = "스케줄을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 완료"),
                    @ApiResponse(responseCode = "404", description = "스케줄 또는 멤버 없음")
            }
    )
    @DeleteMapping("/{scheduleId}")
    void deleteSchedule(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long scheduleId
    );
}
