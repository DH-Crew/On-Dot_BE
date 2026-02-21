package com.dh.ondot.schedule.presentation

import com.dh.ondot.core.TokenInterceptor
import com.dh.ondot.core.exception.GlobalExceptionHandler
import com.dh.ondot.schedule.application.ScheduleCommandFacade
import com.dh.ondot.schedule.application.ScheduleQueryFacade
import com.dh.ondot.schedule.application.dto.UpdateScheduleResult
import com.dh.ondot.schedule.domain.Alarm
import com.dh.ondot.schedule.domain.Place
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.enums.AlarmMode
import com.dh.ondot.schedule.domain.enums.RingTone
import com.dh.ondot.schedule.domain.enums.SnoozeCount
import com.dh.ondot.schedule.domain.enums.SnoozeInterval
import com.dh.ondot.schedule.domain.enums.SoundCategory
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.vo.Snooze
import com.dh.ondot.schedule.domain.vo.Sound
import com.dh.ondot.schedule.presentation.response.HomeScheduleListResponse
import com.dh.ondot.schedule.application.dto.ScheduleParsedResult
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.LocalDateTime

@WebMvcTest(ScheduleController::class)
@Import(GlobalExceptionHandler::class)
@DisplayName("ScheduleController 테스트")
class ScheduleControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var scheduleQueryFacade: ScheduleQueryFacade

    @MockitoBean
    private lateinit var scheduleCommandFacade: ScheduleCommandFacade

    @MockitoBean
    private lateinit var tokenInterceptor: TokenInterceptor

    private val memberId = 1L

    @BeforeEach
    fun setUp() {
        whenever(tokenInterceptor.preHandle(any(), any(), any())).thenReturn(true)
    }

    // ── Helper: 유효한 PlaceDto JSON map ──
    private fun validPlaceMap(title: String = "서울역", roadAddress: String = "서울특별시 용산구") =
        mapOf(
            "title" to title,
            "roadAddress" to roadAddress,
            "longitude" to 126.9726,
            "latitude" to 37.5547,
        )

    // ── Helper: 유효한 PreparationAlarmDto JSON map (for ScheduleCreateRequest) ──
    private fun validPreparationAlarmMap() = mapOf(
        "alarmMode" to "VIBRATE",
        "isEnabled" to true,
        "triggeredAt" to "2025-01-01T09:00:00",
        "isSnoozeEnabled" to true,
        "snoozeInterval" to 5,
        "snoozeCount" to 3,
        "soundCategory" to "BRIGHT_ENERGY",
        "ringTone" to "DANCING_IN_THE_STARDUST",
        "volume" to 0.5,
    )

    // ── Helper: 유효한 DepartureAlarmDto JSON map (for ScheduleCreateRequest) ──
    private fun validDepartureAlarmMap() = mapOf(
        "alarmMode" to "VIBRATE",
        "triggeredAt" to "2025-01-01T09:30:00",
        "isSnoozeEnabled" to true,
        "snoozeInterval" to 5,
        "snoozeCount" to 3,
        "soundCategory" to "BRIGHT_ENERGY",
        "ringTone" to "DANCING_IN_THE_STARDUST",
        "volume" to 0.5,
    )

    // ── Helper: 유효한 ScheduleCreateRequest body map ──
    private fun validScheduleCreateBody() = mapOf(
        "title" to "병원 방문",
        "isRepeat" to false,
        "repeatDays" to listOf<Int>(),
        "appointmentAt" to "2025-01-01T10:00:00",
        "isMedicationRequired" to true,
        "preparationNote" to "약 챙기기",
        "departurePlace" to validPlaceMap("출발지", "서울시 강남구"),
        "arrivalPlace" to validPlaceMap("도착지", "서울시 서초구"),
        "preparationAlarm" to validPreparationAlarmMap(),
        "departureAlarm" to validDepartureAlarmMap(),
    )

    // ── Helper: 유효한 ScheduleUpdateRequest body map ──
    private fun validScheduleUpdateBody() = mapOf(
        "title" to "병원 방문 수정",
        "isRepeat" to false,
        "repeatDays" to listOf<Int>(),
        "appointmentAt" to "2025-01-01T11:00:00",
        "departurePlace" to validPlaceMap("출발지", "서울시 강남구"),
        "arrivalPlace" to validPlaceMap("도착지", "서울시 서초구"),
        "preparationAlarm" to validPreparationAlarmMap(),
        "departureAlarm" to validDepartureAlarmMap(),
    )

    // ── Helper: 유효한 QuickScheduleCreateRequest body map ──
    private fun validQuickScheduleBody() = mapOf(
        "appointmentAt" to "2025-01-01T10:00:00",
        "departurePlace" to validPlaceMap("출발지", "서울시 강남구"),
        "arrivalPlace" to validPlaceMap("도착지", "서울시 서초구"),
    )

    // ── Helper: Schedule mock with nested Alarm mocks (for createSchedule, updateSchedule, switchAlarm) ──
    private fun mockScheduleWithAlarms(): Schedule {
        val snooze: Snooze = mock {
            on { isSnoozeEnabled } doReturn true
            on { snoozeInterval } doReturn SnoozeInterval.FIVE
            on { snoozeCount } doReturn SnoozeCount.THREE
        }
        val sound: Sound = mock {
            on { soundCategory } doReturn SoundCategory.BRIGHT_ENERGY
            on { ringTone } doReturn RingTone.DANCING_IN_THE_STARDUST
            on { volume } doReturn 0.5
        }
        val alarm: Alarm = mock {
            on { id } doReturn 1L
            on { mode } doReturn AlarmMode.VIBRATE
            on { isEnabled } doReturn true
            on { triggeredAt } doReturn Instant.parse("2025-01-01T00:00:00Z")
            on { this.snooze } doReturn snooze
            on { this.sound } doReturn sound
        }
        val place: Place = mock {
            on { title } doReturn "서울역"
            on { roadAddress } doReturn "서울특별시 용산구"
            on { longitude } doReturn 126.9726
            on { latitude } doReturn 37.5547
        }
        return mock {
            on { id } doReturn 1L
            on { title } doReturn "병원 방문"
            on { isRepeat } doReturn false
            on { repeatDays } doReturn null
            on { appointmentAt } doReturn Instant.parse("2025-01-01T01:00:00Z")
            on { createdAt } doReturn Instant.parse("2025-01-01T00:00:00Z")
            on { updatedAt } doReturn Instant.parse("2025-01-01T00:00:00Z")
            on { preparationAlarm } doReturn alarm
            on { departureAlarm } doReturn alarm
            on { departurePlace } doReturn place
            on { arrivalPlace } doReturn place
            on { isMedicationRequired } doReturn true
            on { preparationNote } doReturn "약 챙기기"
            on { transportType } doReturn TransportType.PUBLIC_TRANSPORT
        }
    }

    // ═══════════════════════════════════════════════════
    // POST /schedules — createSchedule
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /schedules")
    inner class CreateSchedule {

        @Test
        @DisplayName("정상 요청 시 201과 ScheduleCreateResponse를 반환한다")
        fun success_201() {
            val schedule = mockScheduleWithAlarms()
            whenever(scheduleCommandFacade.createSchedule(any(), any())).thenReturn(schedule)

            mockMvc.perform(
                post("/schedules")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validScheduleCreateBody()))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.scheduleId").value(1))

            verify(scheduleCommandFacade).createSchedule(any(), any())
        }

        @Test
        @DisplayName("title이 빈값이면 400을 반환한다")
        fun emptyTitle_400() {
            val body = validScheduleCreateBody().toMutableMap().apply { this["title"] = "" }

            mockMvc.perform(
                post("/schedules")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }
    }

    // ═══════════════════════════════════════════════════
    // POST /schedules/quick — createQuickSchedule
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /schedules/quick")
    inner class CreateQuickSchedule {

        @Test
        @DisplayName("정상 요청 시 202를 반환한다")
        fun success_202() {
            mockMvc.perform(
                post("/schedules/quick")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validQuickScheduleBody()))
            ).andExpect(status().isAccepted)

            verify(scheduleCommandFacade).createQuickSchedule(any(), any())
        }
    }

    // ═══════════════════════════════════════════════════
    // POST /schedules/quickV1 — createQuickScheduleV1
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /schedules/quickV1")
    inner class CreateQuickScheduleV1 {

        @Test
        @DisplayName("정상 요청 시 202를 반환한다")
        fun success_202() {
            mockMvc.perform(
                post("/schedules/quickV1")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validQuickScheduleBody()))
            ).andExpect(status().isAccepted)

            verify(scheduleCommandFacade).createQuickScheduleV1(any(), any())
        }
    }

    // ═══════════════════════════════════════════════════
    // POST /schedules/voice — parseVoiceSchedule
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /schedules/voice")
    inner class ParseVoiceSchedule {

        @Test
        @DisplayName("정상 요청 시 200과 ScheduleParsedResponse를 반환한다")
        fun success_200() {
            val result = ScheduleParsedResult("서울역", LocalDateTime.of(2025, 1, 1, 10, 0))
            whenever(scheduleCommandFacade.parseVoiceSchedule(any(), any())).thenReturn(result)

            mockMvc.perform(
                post("/schedules/voice")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("text" to "내일 10시에 서울역 가야 해")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.departurePlaceTitle").value("서울역"))
        }

        @Test
        @DisplayName("text가 빈값이면 400을 반환한다")
        fun emptyText_400() {
            mockMvc.perform(
                post("/schedules/voice")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("text" to "")))
            ).andExpect(status().isBadRequest)
        }
    }

    // ═══════════════════════════════════════════════════
    // POST /schedules/estimate-time — estimateTravelTime
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /schedules/estimate-time")
    inner class EstimateTravelTime {

        @Test
        @DisplayName("정상 요청 시 200과 EstimateTimeResponse를 반환한다")
        fun success_200() {
            whenever(scheduleQueryFacade.estimateTravelTime(any(), any(), any(), any(), any())).thenReturn(30)

            mockMvc.perform(
                post("/schedules/estimate-time")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "startLongitude" to 126.9726,
                                "startLatitude" to 37.5547,
                                "endLongitude" to 127.0276,
                                "endLatitude" to 37.4979,
                            )
                        )
                    )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.estimatedTime").value(30))
        }

        @Test
        @DisplayName("startLongitude가 범위를 초과하면 400을 반환한다")
        fun coordinateExceedsRange_400() {
            mockMvc.perform(
                post("/schedules/estimate-time")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            mapOf(
                                "startLongitude" to 181.0,
                                "startLatitude" to 37.5547,
                                "endLongitude" to 127.0276,
                                "endLatitude" to 37.4979,
                            )
                        )
                    )
            ).andExpect(status().isBadRequest)
        }
    }

    // ═══════════════════════════════════════════════════
    // GET /schedules/{scheduleId} — getSchedule
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /schedules/{scheduleId}")
    inner class GetSchedule {

        @Test
        @DisplayName("정상 요청 시 200을 반환한다")
        fun success_200() {
            val schedule = mockScheduleWithAlarms()
            given(scheduleQueryFacade.findOneByMemberAndSchedule(any(), any())).willReturn(schedule)

            mockMvc.perform(
                get("/schedules/1").requestAttr("memberId", memberId)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.scheduleId").value(1))
                .andExpect(jsonPath("$.title").value("병원 방문"))
        }
    }

    // ═══════════════════════════════════════════════════
    // GET /schedules/{scheduleId}/preparation — getPreparationInfo
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /schedules/{scheduleId}/preparation")
    inner class GetPreparationInfo {

        @Test
        @DisplayName("정상 요청 시 200과 SchedulePreparationResponse를 반환한다")
        fun success_200() {
            val schedule = mockScheduleWithAlarms()
            given(scheduleQueryFacade.findOne(any())).willReturn(schedule)

            mockMvc.perform(
                get("/schedules/1/preparation")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.isMedicationRequired").value(true))
                .andExpect(jsonPath("$.preparationNote").value("약 챙기기"))
        }
    }

    // ═══════════════════════════════════════════════════
    // GET /schedules/{scheduleId}/issues — getScheduleIssues
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /schedules/{scheduleId}/issues")
    inner class GetScheduleIssues {

        @Test
        @DisplayName("정상 요청 시 200과 이슈 문자열을 반환한다")
        fun success_200() {
            given(scheduleQueryFacade.getIssues(any())).willReturn("현재 이슈 없음")

            mockMvc.perform(
                get("/schedules/1/issues")
            )
                .andExpect(status().isOk)
        }
    }

    // ═══════════════════════════════════════════════════
    // GET /schedules — getActiveSchedules
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /schedules")
    inner class GetActiveSchedules {

        @Test
        @DisplayName("정상 요청 시 200과 HomeScheduleListResponse를 반환한다")
        fun success_200() {
            val response = HomeScheduleListResponse(
                earliestAlarmId = 1L,
                earliestAlarmAt = LocalDateTime.of(2025, 1, 1, 9, 0),
                scheduleList = emptyList(),
                hasNext = false,
            )
            whenever(scheduleQueryFacade.findAllActiveSchedules(any(), any())).thenReturn(response)

            mockMvc.perform(
                get("/schedules")
                    .requestAttr("memberId", memberId)
                    .param("page", "0")
                    .param("size", "20")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.earliestAlarmId").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
        }
    }

    // ═══════════════════════════════════════════════════
    // PUT /schedules/{scheduleId} — updateSchedule
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /schedules/{scheduleId}")
    inner class UpdateSchedule {

        @Test
        @DisplayName("재계산 불필요 시 200을 반환한다")
        fun success_200() {
            val schedule = mockScheduleWithAlarms()
            val result = UpdateScheduleResult(schedule, needsDepartureTimeRecalculation = false)
            whenever(scheduleCommandFacade.updateSchedule(any(), any(), any())).thenReturn(result)

            mockMvc.perform(
                put("/schedules/1")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validScheduleUpdateBody()))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.scheduleId").value(1))
        }

        @Test
        @DisplayName("재계산 필요 시 202를 반환한다")
        fun accepted_202() {
            val schedule = mockScheduleWithAlarms()
            val result = UpdateScheduleResult(schedule, needsDepartureTimeRecalculation = true)
            whenever(scheduleCommandFacade.updateSchedule(any(), any(), any())).thenReturn(result)

            mockMvc.perform(
                put("/schedules/1")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validScheduleUpdateBody()))
            )
                .andExpect(status().isAccepted)
                .andExpect(jsonPath("$.scheduleId").value(1))
        }

        @Test
        @DisplayName("title이 빈값이면 400을 반환한다")
        fun emptyTitle_400() {
            val body = validScheduleUpdateBody().toMutableMap().apply { this["title"] = "" }

            mockMvc.perform(
                put("/schedules/1")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }
    }

    // ═══════════════════════════════════════════════════
    // PATCH /schedules/{scheduleId}/alarm — switchAlarm
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("PATCH /schedules/{scheduleId}/alarm")
    inner class SwitchAlarm {

        @Test
        @DisplayName("정상 요청 시 200과 AlarmSwitchResponse를 반환한다")
        fun success_200() {
            val schedule = mockScheduleWithAlarms()
            whenever(scheduleCommandFacade.switchAlarm(any(), any(), any())).thenReturn(schedule)

            mockMvc.perform(
                patch("/schedules/1/alarm")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("isEnabled" to true)))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.scheduleId").value(1))
                .andExpect(jsonPath("$.isEnabled").value(true))
        }
    }

    // ═══════════════════════════════════════════════════
    // DELETE /schedules/{scheduleId} — deleteSchedule
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /schedules/{scheduleId}")
    inner class DeleteSchedule {

        @Test
        @DisplayName("정상 요청 시 204를 반환한다")
        fun success_204() {
            mockMvc.perform(
                delete("/schedules/1").requestAttr("memberId", memberId)
            ).andExpect(status().isNoContent)

            verify(scheduleCommandFacade).deleteSchedule(memberId, 1L)
        }
    }
}
