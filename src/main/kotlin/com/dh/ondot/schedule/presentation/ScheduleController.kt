package com.dh.ondot.schedule.presentation

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
import com.dh.ondot.schedule.presentation.swagger.ScheduleSwagger
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.application.ScheduleCommandFacade
import com.dh.ondot.schedule.application.ScheduleQueryFacade
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/schedules")
class ScheduleController(
    private val scheduleQueryFacade: ScheduleQueryFacade,
    private val scheduleCommandFacade: ScheduleCommandFacade,
) : ScheduleSwagger {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    override fun createSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: ScheduleCreateRequest,
    ): ScheduleCreateResponse {
        val schedule = scheduleCommandFacade.createSchedule(memberId, request.toCommand())
        return ScheduleCreateResponse.of(schedule)
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/quick")
    override fun createQuickSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: QuickScheduleCreateRequest,
    ) {
        scheduleCommandFacade.createQuickSchedule(memberId, request.toCommand())
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/quickV1")
    fun createQuickScheduleV1(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: QuickScheduleCreateRequest,
    ) {
        scheduleCommandFacade.createQuickScheduleV1(memberId, request.toCommand())
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/voice")
    override fun parseVoiceSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: ScheduleParsedRequest,
    ): ScheduleParsedResponse {
        val result = scheduleCommandFacade.parseVoiceSchedule(memberId, request.text)
        return ScheduleParsedResponse.from(result)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/estimate-time")
    override fun estimateTravelTime(
        @Valid @RequestBody request: EstimateTimeRequest,
    ): EstimateTimeResponse {
        val estimatedTime = scheduleQueryFacade.estimateTravelTime(
            request.startLongitude, request.startLatitude,
            request.endLongitude, request.endLatitude,
            request.transportType ?: TransportType.PUBLIC_TRANSPORT,
            request.appointmentAt,
        )
        return EstimateTimeResponse.from(estimatedTime)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}")
    override fun getSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
    ): ScheduleDetailResponse {
        val schedule = scheduleQueryFacade.findOneByMemberAndSchedule(memberId, scheduleId)
        return ScheduleDetailResponse.from(schedule)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}/preparation")
    override fun getPreparationInfo(
        @PathVariable scheduleId: Long,
    ): SchedulePreparationResponse {
        val schedule = scheduleQueryFacade.findOne(scheduleId)
        return SchedulePreparationResponse.from(schedule)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}/issues")
    override fun getScheduleIssues(
        @PathVariable scheduleId: Long,
    ): String {
        return scheduleQueryFacade.getIssues(scheduleId)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    override fun getActiveSchedules(
        @RequestAttribute("memberId") memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): HomeScheduleListResponse {
        val pageable = PageRequest.of(page, size)
        return scheduleQueryFacade.findAllActiveSchedules(memberId, pageable)
    }

    @PutMapping("/{scheduleId}")
    override fun updateSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
        @Valid @RequestBody request: ScheduleUpdateRequest,
    ): ResponseEntity<ScheduleUpdateResponse> {
        val result = scheduleCommandFacade.updateSchedule(memberId, scheduleId, request.toCommand())
        val status = if (result.needsDepartureTimeRecalculation) HttpStatus.ACCEPTED else HttpStatus.OK
        return ResponseEntity.status(status).body(ScheduleUpdateResponse.of(result.schedule))
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{scheduleId}/alarm")
    override fun switchAlarm(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
        @Valid @RequestBody request: AlarmSwitchRequest,
    ): AlarmSwitchResponse {
        val schedule = scheduleCommandFacade.switchAlarm(memberId, scheduleId, request.isEnabled)
        return AlarmSwitchResponse.from(schedule)
    }

    @Operation(
        summary = "에브리타임 URL 검증",
        description = """
            에브리타임 공유 URL의 유효성을 검증합니다.
            - URL 형식 검증 (everytime.kr 도메인, /@{identifier} 경로)
            - 실제 에브리타임 API 호출을 통한 시간표 존재 여부 확인

            **⚠️ Error Codes**
            - URL 형식 오류: `EVERYTIME_INVALID_URL`
            - 시간표를 찾을 수 없음 (비공개/삭제): `EVERYTIME_NOT_FOUND`
            - 수업이 없는 시간표: `EVERYTIME_EMPTY_TIMETABLE`
            - 에브리타임 서버 오류: `EVERYTIME_SERVER_ERROR`
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
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
                description = "검증 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = EverytimeValidateResponse::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "identifier": "ip9ktZ3A7H35H6P7Z1Wr",
                          "isValid": true
                        }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "URL 형식 오류",
                content = [Content(
                    mediaType = "application/json",
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
                    mediaType = "application/json",
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
                    mediaType = "application/json",
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
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/everytime/validate")
    fun validateEverytimeUrl(
        @Valid @RequestBody request: EverytimeValidateRequest,
    ): EverytimeValidateResponse {
        val identifier = scheduleCommandFacade.validateEverytimeUrl(request.everytimeUrl)
        return EverytimeValidateResponse(
            identifier = identifier,
            isValid = true,
        )
    }

    @Operation(
        summary = "에브리타임 시간표 기반 스케줄 일괄 생성",
        description = """
            에브리타임 공유 URL을 기반으로 시간표를 조회한 뒤,
            요일별 첫 수업 시작시간을 기준으로 반복 스케줄을 일괄 생성합니다.

            **📌 생성 규칙**
            - 동일한 시작시간의 요일들은 하나의 반복 스케줄로 묶입니다
              (예: 월/수 09:30 → "월/수요일 학교", 화/목 11:00 → "화/목요일 학교")
            - 각 스케줄에는 멤버 기본 알람 설정이 적용됩니다
            - `transportType` 미지정 시 `PUBLIC_TRANSPORT`(대중교통)로 처리

            **🚗 경로 계산**
            - 대중교통: 1회 조회 후 전체 그룹에 재사용
            - 자가용: 시간대별 조회 (동일 시간 그룹은 첫 번째 요일 기준)

            **⚠️ Error Codes**
            - URL 형식 오류: `EVERYTIME_INVALID_URL`
            - 시간표를 찾을 수 없음: `EVERYTIME_NOT_FOUND`
            - 수업이 없는 시간표: `EVERYTIME_EMPTY_TIMETABLE`
            - 에브리타임 서버 오류: `EVERYTIME_SERVER_ERROR`
            - 경로 계산 오류: `ODSAY_*`, `TMAP_*` 계열 에러 코드
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = EverytimeScheduleCreateRequest::class),
                examples = [ExampleObject(
                    name = "예시-요청",
                    value = """
                    {
                      "everytimeUrl": "https://everytime.kr/@ip9ktZ3A7H35H6P7Z1Wr",
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
                responseCode = "201",
                description = "스케줄 일괄 생성 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = EverytimeScheduleCreateResponse::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "createdCount": 2,
                          "schedules": [
                            {
                              "scheduleId": 101,
                              "title": "월/수요일 학교",
                              "repeatDays": [2, 4],
                              "appointmentAt": "2026-02-23T09:30:00Z"
                            },
                            {
                              "scheduleId": 102,
                              "title": "화/목요일 학교",
                              "repeatDays": [3, 5],
                              "appointmentAt": "2026-02-24T11:00:00Z"
                            }
                          ]
                        }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "URL 형식 오류 또는 검증 오류",
                content = [Content(
                    mediaType = "application/json",
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
                description = "시간표를 찾을 수 없음 또는 멤버 없음",
                content = [Content(
                    mediaType = "application/json",
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
                description = "에브리타임 또는 경로 API 서버 장애",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(ref = "#/components/schemas/ErrorResponse"),
                    examples = [ExampleObject(
                        value = """
                        {
                          "errorCode": "EVERYTIME_SERVER_ERROR",
                          "message": "에브리타임 서버에 일시적인 오류가 발생했습니다: timeout"
                        }"""
                    )]
                )]
            )
        ]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/everytime")
    fun createSchedulesFromEverytime(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: EverytimeScheduleCreateRequest,
    ): EverytimeScheduleCreateResponse {
        val transportType = request.transportType ?: TransportType.PUBLIC_TRANSPORT
        val schedules = scheduleCommandFacade.createSchedulesFromEverytime(
            memberId,
            request.everytimeUrl,
            request.startLongitude, request.startLatitude,
            request.endLongitude, request.endLatitude,
            transportType,
        )
        return EverytimeScheduleCreateResponse(
            createdCount = schedules.size,
            schedules = schedules.map { schedule ->
                EverytimeScheduleCreateResponse.EverytimeScheduleItem(
                    scheduleId = schedule.id,
                    title = schedule.title,
                    repeatDays = schedule.repeatDays?.toList() ?: emptyList(),
                    appointmentAt = schedule.appointmentAt.toString(),
                )
            },
        )
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{scheduleId}")
    override fun deleteSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
    ) {
        scheduleCommandFacade.deleteSchedule(memberId, scheduleId)
    }
}
