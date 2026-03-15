package com.dh.ondot.schedule.presentation.swagger

import com.dh.ondot.schedule.presentation.response.CalendarDailyResponse
import com.dh.ondot.schedule.presentation.response.CalendarRangeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import java.time.LocalDate

@Tag(name = "Calendar API", description = "캘린더 조회 및 기록 관리 API")
interface CalendarSwagger {

    @Operation(
        summary = "캘린더 범위 조회",
        description = """
시작일~종료일 범위의 스케줄을 날짜별로 그룹핑하여 조회한다.

- 최대 45일 범위
- 약속 시간이 현재 시각 이전이면 RECORD, 이후이면 ALARM
- 반복 일정은 해당 요일마다 확장하여 표시
- 삭제된 일정은 삭제 이전 기록만 RECORD로 표시
- 일정이 없는 날짜는 응답에 포함되지 않음
        """,
        responses = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CalendarRangeResponse::class),
                    examples = [ExampleObject(
                        name = "범위 조회 응답",
                        value = """
{
  "days": [
    {
      "date": "2026-03-14",
      "schedules": [
        {
          "scheduleId": 1,
          "title": "출근",
          "type": "RECORD",
          "isRepeat": true,
          "appointmentAt": "2026-03-14T09:00"
        },
        {
          "scheduleId": 2,
          "title": "병원 예약",
          "type": "RECORD",
          "isRepeat": false,
          "appointmentAt": "2026-03-14T14:00"
        }
      ]
    },
    {
      "date": "2026-03-16",
      "schedules": [
        {
          "scheduleId": 3,
          "title": "저녁 약속",
          "type": "ALARM",
          "isRepeat": false,
          "appointmentAt": "2026-03-16T18:30"
        }
      ]
    }
  ]
}
                        """
                    )]
                )],
            ),
            ApiResponse(responseCode = "400", description = "날짜 범위 오류 (startDate > endDate 또는 45일 초과)"),
        ],
    )
    fun getCalendarRange(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "시작일 (yyyy-MM-dd)", example = "2026-03-01") startDate: LocalDate,
        @Parameter(description = "종료일 (yyyy-MM-dd)", example = "2026-03-31") endDate: LocalDate,
    ): CalendarRangeResponse

    @Operation(
        summary = "캘린더 일별 조회",
        description = """
특정 날짜의 스케줄을 상세 조회한다.

- 알람 정보, 출발/도착 좌표, 준비 메모 등 상세 정보 포함
- 시간 빠른 순 정렬
        """,
        responses = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CalendarDailyResponse::class),
                    examples = [ExampleObject(
                        name = "일별 조회 응답",
                        value = """
{
  "schedules": [
    {
      "scheduleId": 1,
      "type": "RECORD",
      "title": "출근",
      "isRepeat": true,
      "repeatDays": [2, 3, 4, 5, 6],
      "appointmentAt": "2026-03-14T09:00",
      "preparationAlarm": {
        "alarmId": 10,
        "alarmMode": "SOUND",
        "isEnabled": true,
        "triggeredAt": "2026-03-14T08:00",
        "isSnoozeEnabled": true,
        "snoozeInterval": 5,
        "snoozeCount": 3,
        "soundCategory": "BRIGHT_ENERGY",
        "ringTone": "DANCING_IN_THE_STARDUST",
        "volume": 0.5
      },
      "departureAlarm": {
        "alarmId": 11,
        "alarmMode": "SOUND",
        "isEnabled": true,
        "triggeredAt": "2026-03-14T08:30",
        "isSnoozeEnabled": true,
        "snoozeInterval": 5,
        "snoozeCount": 3,
        "soundCategory": "BRIGHT_ENERGY",
        "ringTone": "DANCING_IN_THE_STARDUST",
        "volume": 0.5
      },
      "hasActiveAlarm": true,
      "startLongitude": 127.0276,
      "startLatitude": 37.4979,
      "endLongitude": 126.9780,
      "endLatitude": 37.5665,
      "preparationNote": "우산 챙기기"
    }
  ]
}
                        """
                    )]
                )],
            ),
        ],
    )
    fun getCalendarDaily(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "조회일 (yyyy-MM-dd)", example = "2026-03-14") date: LocalDate,
    ): CalendarDailyResponse

    @Operation(
        summary = "캘린더 기록 삭제",
        description = """
특정 날짜의 과거 기록을 캘린더에서 제거한다.

- 이미 삭제된 기록을 다시 삭제해도 오류 없음 (멱등성 보장)
- 삭제된 스케줄의 과거 기록도 삭제 가능
        """,
        responses = [
            ApiResponse(responseCode = "204", description = "삭제 성공 (본문 없음)"),
            ApiResponse(responseCode = "404", description = "스케줄 미존재"),
        ],
    )
    fun deleteCalendarRecord(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "스케줄 ID", example = "1") scheduleId: Long,
        @Parameter(description = "제외할 날짜 (yyyy-MM-dd)", example = "2026-03-14") date: LocalDate,
    )
}
