package com.dh.ondot.schedule.presentation.swagger

import com.dh.ondot.schedule.presentation.request.RecordAlarmTriggerRequest
import com.dh.ondot.schedule.presentation.request.SetAlarmRequest
import com.dh.ondot.schedule.presentation.response.SettingAlarmResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

/*──────────────────────────────────────────────────────────────
 * Alarm Swagger
 *──────────────────────────────────────────────────────────────*/
@Tag(
    name = "Alarm API",
    description = """
        **AccessToken (Authorization: Bearer JWT)** 은 필수입니다.

        **🔔 Alarm ENUM**
        - `AlarmMode`: SILENT, VIBRATE, SOUND
        - `SnoozeInterval`: 1, 3, 5, 10, 30, 60 (분)
        - `SnoozeCount`: -1(INFINITE), 1, 3, 5, 10 (회)
        - `SoundCategory`: *BRIGHT_ENERGY, FAST_INTENSE*
        - `RingTone`: *DANCING_IN_THE_STARDUST, IN_THE_CITY_LIGHTS_MIST, FRACTURED_LOVE, CHASING_LIGHTS, ASHES_OF_US, HEATING_SUN, NO_COPYRIGHT_MUSIC, MEDAL, EXCITING_SPORTS_COMPETITIONS, POSITIVE_WAY, ENERGETIC_HAPPY_UPBEAT_ROCK_MUSIC, ENERGY_CATCHER*
        - `AlarmTriggerAction`: SCHEDULED, STOP, SNOOZE, VIEW_ROUTE, START_PREPARE
        """
)
@RequestMapping("/alarms")
interface AlarmSwagger {

    /*──────────────────────────────────────────────────────
     * 1. 출도착지 기반 알람 세팅
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "출도착지 기반 알람 세팅",
        description = """
            출도착지를 기반으로 예상시간을 계산합니다.
            사용자의 스케줄 중 `updatedAt`이 가장 최신인 1건을 기준으로
            **준비 알람**과 **출발 알람** 설정 값을 반환합니다.
            최신 스케줄이 없는 경우 온보딩에서 설정한 값을 가져옵니다.

            **📌 파라미터 설명**
            - `transportType`: `PUBLIC_TRANSPORT`(대중교통, 기본값) 또는 `CAR`(자가용)
            - `appointmentAt`: 약속 시간. 자가용(`CAR`) 선택 시 해당 시간대의 예측 교통량을 반영합니다.

            **⚠️ Error Codes (대중교통)**
            - 요청 JSON 문법 오류: `INVALID_JSON`
            - 입력 필드 검증 실패: `FIELD_ERROR`
            - 좌표 형식·범위 오류: `TMAP_TRANSIT_BAD_INPUT`, `TMAP_TRANSIT_MISSING_PARAM`
            - 대중교통 경로 없음 (도보 폴백): `TMAP_TRANSIT_NO_ROUTE`
            - 서비스 지역 아님: `TMAP_TRANSIT_SERVICE_AREA`
            - TMAP 대중교통 서버 오류: `TMAP_TRANSIT_SERVER_ERROR`
            - 예기치 못한 TMAP 대중교통 오류: `TMAP_TRANSIT_UNHANDLED_ERROR`

            **⚠️ Error Codes (자가용)**
            - TMAP 서버 오류: `TMAP_SERVER_ERROR`
            - TMAP 결과 없음: `TMAP_NO_RESULT`
            - 그 외 서버 오류: `SERVER_ERROR`
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "약속 시간과 출발·도착 좌표를 담은 요청 바디",
            content = [Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = Schema(implementation = SetAlarmRequest::class),
                examples = [ExampleObject(
                    name = "예시-요청",
                    value = """
                        {
                          "appointmentAt": "2025-04-16T18:00:00",
                          "startLongitude": 127.070593415212,
                          "startLatitude": 37.277975571288,
                          "endLongitude": 126.94569176914,
                          "endLatitude": 37.5959199688468,
                          "transportType": "PUBLIC_TRANSPORT"
                        }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    schema = Schema(implementation = SettingAlarmResponse::class),
                    examples = [ExampleObject(
                        value = """
                        {
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
                            "isEnabled": true,
                            "triggeredAt": "2025-05-10T18:50:00",
                            "isSnoozeEnabled": false,
                            "snoozeInterval": 0,
                            "snoozeCount": -1,
                            "soundCategory": "BRIGHT_ENERGY",
                            "ringTone": "FRACTURED_LOVE",
                            "volume": 0.2
                          }
                        }"""
                    )]
                )]
            ),
            ApiResponse(responseCode = "404", description = "스케줄 없음")
        ]
    )
    @PostMapping("/setting")
    @ApiResponse(responseCode = "200")
    fun setAlarm(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: SetAlarmRequest,
    ): SettingAlarmResponse

    /*──────────────────────────────────────────────────────
     * 2. 알람 트리거 기록 저장
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "알람 트리거 기록 저장",
        description = """
            알람이 실제로 울렸을 때의 기록을 저장합니다.
            사용자가 알람에 대해 취한 액션(끔/다시알림/무응답)과 응답 시간 등의 지표를 수집합니다.

            **📝 action 필드 가능한 값**
            - `SCHEDULED`: 스케줄링 등록
            - `STOP`: 알람 끔
            - `SNOOZE`: 다시 알림
            - `VIEW_ROUTE`: 경로안내 보기
            - `START_PREPARE`: 준비 시작하기

            **⚠️ Error Codes**
            - 요청 JSON 문법 오류: `INVALID_JSON`
            - 입력 필드 검증 실패: `FIELD_ERROR`
            - 알람을 찾을 수 없음: `NOT_FOUND_ALARM`
            - 잘못된 알람 트리거 액션: `INVALID_ALARM_TRIGGER_ACTION`
            - 그 외 서버 오류: `SERVER_ERROR`
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "알람 트리거 정보 (scheduleId, alarmId, action 포함)",
            content = [Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = Schema(implementation = RecordAlarmTriggerRequest::class),
                examples = [ExampleObject(
                    name = "예시-요청",
                    value = """
                        {
                          "scheduleId": 789,
                          "alarmId": 456,
                          "action": "STOP"
                        }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "기록 저장 성공 (응답 본문 없음)"
            ),
            ApiResponse(responseCode = "404", description = "알람을 찾을 수 없음"),
            ApiResponse(responseCode = "400", description = "잘못된 알람 트리거 액션")
        ]
    )
    @PostMapping("/triggers")
    @ApiResponse(responseCode = "201")
    fun recordAlarmTrigger(
        @RequestAttribute("memberId") memberId: Long,
        @Parameter(
            name = "X-Mobile-Type",
            description = "모바일 디바이스 타입 (예: iOS, Android)",
            required = false,
            example = "iOS"
        )
        @RequestHeader(value = "X-Mobile-Type", required = false) mobileType: String?,
        @RequestBody request: RecordAlarmTriggerRequest,
    )
}
