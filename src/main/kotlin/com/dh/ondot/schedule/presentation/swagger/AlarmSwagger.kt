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
        <b>AccessToken (Authorization: Bearer JWT)</b>은 필수입니다.<br>
        <br>
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
        </i><br>
        • <code>AlarmTriggerAction</code>: SCHEDULED, STOP, SNOOZE, VIEW_ROUTE, START_PREPARE
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
            출도착지를 기반으로 예상시간을 계산합니다.<br>
            사용자의 스케줄 중 <code>updatedAt</code>이 가장 최신인 1건을 기준으로
            <b>준비 알람</b>과 <b>출발 알람</b> 설정 값을 반환합니다.<br/>
            최신 스케줄이 없는 경우 온보딩에서 설정한 값을 가져옵니다.<br/>
            <br/>
            <b>📌 파라미터 설명</b><br/>
            • <code>transportType</code>: <code>PUBLIC_TRANSPORT</code>(대중교통, 기본값) 또는 <code>CAR</code>(자가용)<br/>
            • <code>appointmentAt</code>: 약속 시간. 자가용(<code>CAR</code>) 선택 시 해당 시간대의 예측 교통량을 반영합니다.
            <br/><br/>
            <b>⚠️ Error Codes</b><br/>
            • 요청 JSON 문법 오류: <code>INVALID_JSON</code><br/>
            • 입력 필드 검증 실패: <code>FIELD_ERROR</code><br/>
            • 좌표 형식·범위 오류: <code>ODSAY_BAD_INPUT</code>, <code>ODSAY_MISSING_PARAM</code><br/>
            • 정류장 없음: <code>ODSAY_NO_STOP</code><br/>
            • 서비스 지역 아님: <code>ODSAY_SERVICE_AREA</code><br/>
            • 지나치게 가까움(700m 이내): <code>ODSAY_TOO_CLOSE</code><br/>
            • 검색 결과 없음: <code>ODSAY_NO_RESULT</code><br/>
            • ODsay 서버 내부 오류: <code>ODSAY_SERVER_ERROR</code><br/>
            • 예기치 못한 ODsay 오류: <code>ODSAY_UNHANDLED_ERROR</code><br/>
            • TMAP 서버 오류: <code>TMAP_SERVER_ERROR</code><br/>
            • TMAP 결과 없음: <code>TMAP_NO_RESULT</code><br/>
            • 그 외 서버 오류: <code>SERVER_ERROR</code>
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
            알람이 실제로 울렸을 때의 기록을 저장합니다.<br>
            사용자가 알람에 대해 취한 액션(끔/다시알림/무응답)과 응답 시간 등의 지표를 수집합니다.<br/>
            <br/>
            <b>📝 action 필드 가능한 값</b><br/>
            • <code>SCHEDULED</code>: 스케줄링 등록<br/>
            • <code>STOP</code>: 알람 끔<br/>
            • <code>SNOOZE</code>: 다시 알림<br/>
            • <code>VIEW_ROUTE</code>: 경로안내 보기<br/>
            • <code>START_PREPARE</code>: 준비 시작하기<br/>
            <br/>
            <b>⚠️ Error Codes</b><br/>
            • 요청 JSON 문법 오류: <code>INVALID_JSON</code><br/>
            • 입력 필드 검증 실패: <code>FIELD_ERROR</code><br/>
            • 알람을 찾을 수 없음: <code>NOT_FOUND_ALARM</code><br/>
            • 잘못된 알람 트리거 액션: <code>INVALID_ALARM_TRIGGER_ACTION</code><br/>
            • 그 외 서버 오류: <code>SERVER_ERROR</code>
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
