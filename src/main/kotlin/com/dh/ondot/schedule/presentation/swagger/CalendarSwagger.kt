package com.dh.ondot.schedule.presentation.swagger

import com.dh.ondot.schedule.presentation.response.CalendarDailyResponse
import com.dh.ondot.schedule.presentation.response.CalendarRangeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Calendar API", description = "캘린더 조회 및 기록 관리 API")
interface CalendarSwagger {

    @Operation(summary = "캘린더 범위 조회", description = "시작일~종료일 범위의 스케줄을 날짜별로 그룹핑하여 조회한다. 최대 45일.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "400", description = "날짜 범위 오류")
    fun getCalendarRange(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "시작일 (yyyy-MM-dd)", example = "2026-03-01") startDate: String,
        @Parameter(description = "종료일 (yyyy-MM-dd)", example = "2026-03-31") endDate: String,
    ): CalendarRangeResponse

    @Operation(summary = "캘린더 일별 조회", description = "특정 날짜의 스케줄을 상세 조회한다. 시간 빠른 순 정렬.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    fun getCalendarDaily(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "조회일 (yyyy-MM-dd)", example = "2026-03-14") date: String,
    ): CalendarDailyResponse

    @Operation(summary = "캘린더 기록 삭제", description = "특정 날짜의 과거 기록을 캘린더에서 제거한다. 멱등성 보장.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "스케줄 미존재")
    fun deleteCalendarRecord(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "스케줄 ID") scheduleId: Long,
        @Parameter(description = "제외할 날짜 (yyyy-MM-dd)", example = "2026-03-14") date: String,
    )
}
