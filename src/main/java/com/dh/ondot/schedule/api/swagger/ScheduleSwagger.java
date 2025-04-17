package com.dh.ondot.schedule.api.swagger;

import com.dh.ondot.core.domain.ErrorResponse;
import com.dh.ondot.schedule.api.request.ScheduleCreateRequest;
import com.dh.ondot.schedule.api.request.ScheduleUpdateRequest;
import com.dh.ondot.schedule.api.request.VoiceScheduleCreateRequest;
import com.dh.ondot.schedule.api.response.HomeScheduleListResponse;
import com.dh.ondot.schedule.api.response.ScheduleCreateResponse;
import com.dh.ondot.schedule.api.response.ScheduleDetailResponse;
import com.dh.ondot.schedule.api.response.ScheduleUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        • <code>SoundCategory</code>: <i>DEFAULT …</i><br>
        • <code>RingTone</code>: <i>DEFAULT …</i>
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
                        "soundCategory": "DEFAULT",
                        "ringTone": "DEFAULT",
                        "volume": 7
                      },
                      "departureAlarm": {
                        "alarmMode": "SOUND",
                        "triggeredAt": "2025-05-10T18:50:00",
                        "isSnoozeEnabled": false,
                        "snoozeInterval": 1,
                        "snoozeCount": -1,
                        "soundCategory": "DEFAULT",
                        "ringTone": "DEFAULT",
                        "volume": 8
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
     * 음성(STT) 기반 일정 생성
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "음성 기반 일정 생성",
            description = """
            STT(Speech-to-Text) 음성 인식 결과를 자연어 처리 기반 LLM(GPT)을 통해 구조화된 일정 데이터로 파싱하여 스케줄을 생성합니다.<br>
            알람 관련 시간 계산은 비동기적으로 처리되며, 요청이 성공적으로 수신되면 202 Accepted 상태 코드를 반환합니다.
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = VoiceScheduleCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "voiceCreateRequest",
                                    value = """
                    {
                      "title": "회의",
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
    @PostMapping("/voice")
    void createVoiceSchedule(
            @RequestAttribute("memberId") Long memberId,
            @RequestBody VoiceScheduleCreateRequest request
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
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
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
                        "alarmMode": "VIBRATE",
                        "isEnabled": true,
                        "triggeredAt": "2025-05-10T18:30:00",
                        "isSnoozeEnabled": true,
                        "snoozeInterval": 5,
                        "snoozeCount": 3,
                        "soundCategory": "DEFAULT",
                        "ringTone": "DEFAULT",
                        "volume": 7
                      },
                      "departureAlarm": {
                        "alarmMode": "SOUND",
                        "triggeredAt": "2025-05-10T18:50:00",
                        "isSnoozeEnabled": false,
                        "snoozeInterval": 1,
                        "snoozeCount": -1,
                        "soundCategory": "DEFAULT",
                        "ringTone": "DEFAULT",
                        "volume": 8
                      }
                    }""")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description  = "스케줄 또는 멤버가 존재하지 않습니다.",
                            content      = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
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
            summary     = "홈 화면 일정 목록 조회",
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
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema    = @Schema(implementation = HomeScheduleListResponse.class),
                                    examples  = @ExampleObject(value = """
                    {
                      "isOnboardingCompleted": true,
                      "earliestAlarmAt": "2025-05-10T18:30:00",
                      "hasNext": false,
                      "scheduleList": [
                        {
                          "scheduleId": 1001,
                          "scheduleTitle": "스터디 모임",
                          "isRepeat": true,
                          "repeatDays": [2,4,6],
                          "appointmentAt": "2025-05-10T19:00:00",
                          "nextAlarmAt": "2025-05-10T18:30:00",
                          "preparationTriggeredAt": "2025-05-10T18:30:00",
                          "departureTriggeredAt": "2025-05-10T18:50:00",
                          "isEnabled": true
                        }
                      ]
                    }""")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description  = "멤버 없음",
                            content      = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
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
                        "soundCategory": "DEFAULT",
                        "ringTone": "DEFAULT",
                        "volume": 7
                      },
                      "departureAlarm": {
                        "alarmMode": "SOUND",
                        "triggeredAt": "2025-05-10T19:10:00",
                        "isSnoozeEnabled": false,
                        "snoozeInterval": 1,
                        "snoozeCount": -1,
                        "soundCategory": "DEFAULT",
                        "ringTone": "DEFAULT",
                        "volume": 8
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
