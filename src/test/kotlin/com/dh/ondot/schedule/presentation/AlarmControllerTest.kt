package com.dh.ondot.schedule.presentation

import com.dh.ondot.core.TokenInterceptor
import com.dh.ondot.core.exception.GlobalExceptionHandler
import com.dh.ondot.schedule.application.AlarmFacade
import com.dh.ondot.schedule.domain.Alarm
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.enums.AlarmMode
import com.dh.ondot.schedule.domain.enums.RingTone
import com.dh.ondot.schedule.domain.enums.SnoozeCount
import com.dh.ondot.schedule.domain.enums.SnoozeInterval
import com.dh.ondot.schedule.domain.enums.SoundCategory
import com.dh.ondot.schedule.domain.vo.Snooze
import com.dh.ondot.schedule.domain.vo.Sound
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(AlarmController::class)
@Import(GlobalExceptionHandler::class)
@DisplayName("AlarmController 테스트")
class AlarmControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var alarmFacade: AlarmFacade

    @MockitoBean
    private lateinit var tokenInterceptor: TokenInterceptor

    private val memberId = 1L

    @BeforeEach
    fun setUp() {
        whenever(tokenInterceptor.preHandle(any(), any(), any())).thenReturn(true)
    }

    // ── Helper: Schedule mock with nested Alarm mocks (for setAlarm) ──
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
        return mock {
            on { preparationAlarm } doReturn alarm
            on { departureAlarm } doReturn alarm
        }
    }

    // ── Helper: 유효한 SetAlarmRequest body map ──
    private fun validSetAlarmBody() = mapOf(
        "appointmentAt" to "2025-01-01T10:00:00",
        "startLongitude" to 126.9726,
        "startLatitude" to 37.5547,
        "endLongitude" to 127.0276,
        "endLatitude" to 37.4979,
    )

    // ── Helper: 유효한 RecordAlarmTriggerRequest body map ──
    private fun validRecordTriggerBody() = mapOf(
        "scheduleId" to 1L,
        "alarmId" to 1L,
        "action" to "SCHEDULED",
    )

    // ═══════════════════════════════════════════════════
    // POST /alarms/setting — setAlarm
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /alarms/setting")
    inner class SetAlarm {

        @Test
        @DisplayName("정상 요청 시 200과 SettingAlarmResponse를 반환한다")
        fun success_200() {
            val schedule = mockScheduleWithAlarms()
            whenever(alarmFacade.generateAlarmSettingByRoute(any(), any(), any(), any(), any(), any()))
                .thenReturn(schedule)

            mockMvc.perform(
                post("/alarms/setting")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSetAlarmBody()))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.preparationAlarm.alarmId").value(1))
                .andExpect(jsonPath("$.preparationAlarm.alarmMode").value("VIBRATE"))
                .andExpect(jsonPath("$.departureAlarm.alarmId").value(1))
                .andExpect(jsonPath("$.departureAlarm.alarmMode").value("VIBRATE"))

            verify(alarmFacade).generateAlarmSettingByRoute(any(), any(), any(), any(), any(), any())
        }

        @Test
        @DisplayName("좌표 범위를 초과하면 400을 반환한다")
        fun coordinateExceedsRange_400() {
            val body = validSetAlarmBody().toMutableMap().apply { this["startLongitude"] = 181.0 }

            mockMvc.perform(
                post("/alarms/setting")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }
    }

    // ═══════════════════════════════════════════════════
    // POST /alarms/triggers — recordAlarmTrigger
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /alarms/triggers")
    inner class RecordAlarmTrigger {

        @Test
        @DisplayName("정상 요청 시 201을 반환한다")
        fun success_201() {
            mockMvc.perform(
                post("/alarms/triggers")
                    .requestAttr("memberId", memberId)
                    .header("X-Mobile-Type", "ANDROID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRecordTriggerBody()))
            ).andExpect(status().isCreated)

            verify(alarmFacade).recordAlarmTrigger(any(), any(), any(), any(), any())
        }

        @Test
        @DisplayName("유효하지 않은 action이면 400을 반환한다")
        fun invalidAction_400() {
            val body = validRecordTriggerBody().toMutableMap().apply { this["action"] = "INVALID" }

            mockMvc.perform(
                post("/alarms/triggers")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("scheduleId가 문자열이면 400을 반환한다")
        fun invalidScheduleId_400() {
            mockMvc.perform(
                post("/alarms/triggers")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"scheduleId":"abc","alarmId":1,"action":"SCHEDULED"}""")
            ).andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("alarmId가 문자열이면 400을 반환한다")
        fun invalidAlarmId_400() {
            mockMvc.perform(
                post("/alarms/triggers")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"scheduleId":1,"alarmId":"abc","action":"SCHEDULED"}""")
            ).andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("action이 누락되면 400을 반환한다")
        fun missingAction_400() {
            val body = validRecordTriggerBody().toMutableMap().apply { remove("action") }

            mockMvc.perform(
                post("/alarms/triggers")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }
    }
}
