package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.application.dto.EverytimeLecture
import com.dh.ondot.schedule.application.mapper.QuickScheduleMapper
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.AiUsageService
import com.dh.ondot.schedule.domain.service.PlaceService
import com.dh.ondot.schedule.domain.service.ScheduleQueryService
import com.dh.ondot.schedule.domain.service.ScheduleService
import com.dh.ondot.schedule.fixture.MemberFixture
import com.dh.ondot.schedule.fixture.MockitoHelper.anyNonNull
import com.dh.ondot.schedule.fixture.MockitoHelper.eqNonNull
import com.dh.ondot.schedule.infra.api.EverytimeApi
import com.dh.ondot.schedule.infra.api.OpenAiPromptApi
import com.dh.ondot.schedule.infra.exception.EverytimeEmptyTimetableException
import com.dh.ondot.schedule.infra.exception.EverytimeInvalidUrlException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
@DisplayName("ScheduleCommandFacade 에브리타임 테스트")
class ScheduleCommandFacadeEverytimeTest {

    @Mock private lateinit var memberService: MemberService
    @Mock private lateinit var scheduleService: ScheduleService
    @Mock private lateinit var scheduleQueryService: ScheduleQueryService
    @Mock private lateinit var routeService: RouteService
    @Mock private lateinit var placeService: PlaceService
    @Mock private lateinit var aiUsageService: AiUsageService
    @Mock private lateinit var quickScheduleMapper: QuickScheduleMapper
    @Mock private lateinit var everytimeApi: EverytimeApi
    @Mock private lateinit var openAiPromptApi: OpenAiPromptApi
    @Mock private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var facade: ScheduleCommandFacade

    @Nested
    @DisplayName("validateEverytimeUrl")
    inner class ValidateEverytimeUrlTest {

        @Test
        @DisplayName("유효한 에브리타임 URL에서 identifier를 추출한다")
        fun validateEverytimeUrl_ValidUrl_ReturnsIdentifier() {
            // given
            val url = "https://everytime.kr/@ip9ktZ3A7H35H6P7Z1Wr"
            given(everytimeApi.fetchTimetable("ip9ktZ3A7H35H6P7Z1Wr"))
                .willReturn(listOf(createLecture("테스트", 0, "09:30", "10:45")))

            // when
            val result = facade.validateEverytimeUrl(url)

            // then
            assertThat(result).isEqualTo("ip9ktZ3A7H35H6P7Z1Wr")
        }

        @Test
        @DisplayName("everytime.kr이 아닌 도메인일 경우 EverytimeInvalidUrlException이 발생한다")
        fun validateEverytimeUrl_WrongDomain_ThrowsInvalidUrlException() {
            assertThatThrownBy { facade.validateEverytimeUrl("https://example.com/@someIdentifier") }
                .isInstanceOf(EverytimeInvalidUrlException::class.java)
        }

        @Test
        @DisplayName("/@로 시작하지 않는 경로일 경우 EverytimeInvalidUrlException이 발생한다")
        fun validateEverytimeUrl_WrongPath_ThrowsInvalidUrlException() {
            assertThatThrownBy { facade.validateEverytimeUrl("https://everytime.kr/timetable/123") }
                .isInstanceOf(EverytimeInvalidUrlException::class.java)
        }

        @Test
        @DisplayName("identifier가 비어있을 경우 EverytimeInvalidUrlException이 발생한다")
        fun validateEverytimeUrl_EmptyIdentifier_ThrowsInvalidUrlException() {
            assertThatThrownBy { facade.validateEverytimeUrl("https://everytime.kr/@") }
                .isInstanceOf(EverytimeInvalidUrlException::class.java)
        }

        @Test
        @DisplayName("잘못된 URL 형식일 경우 EverytimeInvalidUrlException이 발생한다")
        fun validateEverytimeUrl_MalformedUrl_ThrowsInvalidUrlException() {
            assertThatThrownBy { facade.validateEverytimeUrl("not-a-valid-url") }
                .isInstanceOf(EverytimeInvalidUrlException::class.java)
        }
    }

    @Nested
    @DisplayName("createSchedulesFromEverytime")
    inner class CreateSchedulesFromEverytimeTest {

        @Test
        @DisplayName("동일 시간대의 수업이 있는 요일들은 하나의 반복 스케줄로 묶인다")
        fun createSchedules_SameTimeDays_GroupedIntoOneSchedule() {
            // given
            val member = MemberFixture.defaultMember()
            val lectures = listOf(
                createLecture("수학", 0, "09:30", "10:45"),
                createLecture("물리", 2, "09:30", "10:45"),
                createLecture("영어", 1, "11:00", "12:15"),
                createLecture("국어", 3, "11:00", "12:15"),
            )

            given(memberService.getMemberIfExists(1L)).willReturn(member)
            given(everytimeApi.fetchTimetable("testId")).willReturn(lectures)
            given(routeService.calculateRouteTime(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyNonNull(), anyNonNull(),
            )).willReturn(30)
            given(scheduleService.createEverytimeSchedule(
                anyNonNull(), anyNonNull(), anyNonNull(), anyNonNull(),
                anyNonNull(), anyNonNull(), anyInt(), anyNonNull(),
            )).willReturn(Schedule())

            // when
            val result = facade.createSchedulesFromEverytime(
                1L, "https://everytime.kr/@testId",
                127.0, 37.0, 126.9, 37.5, TransportType.PUBLIC_TRANSPORT,
            )

            // then
            assertThat(result).hasSize(2)
        }

        @Test
        @DisplayName("빈 시간표일 경우 EverytimeEmptyTimetableException이 발생한다")
        fun createSchedules_EmptyTimetable_ThrowsException() {
            // given
            val member = MemberFixture.defaultMember()
            given(memberService.getMemberIfExists(1L)).willReturn(member)
            given(everytimeApi.fetchTimetable("emptyId")).willReturn(emptyList())

            // when & then
            assertThatThrownBy {
                facade.createSchedulesFromEverytime(
                    1L, "https://everytime.kr/@emptyId",
                    127.0, 37.0, 126.9, 37.5, TransportType.PUBLIC_TRANSPORT,
                )
            }.isInstanceOf(EverytimeEmptyTimetableException::class.java)
        }

        @Test
        @DisplayName("대중교통 경로 계산은 1회만 호출된다")
        fun createSchedules_PublicTransport_CalculatesRouteOnce() {
            // given
            val member = MemberFixture.defaultMember()
            val lectures = listOf(
                createLecture("수학", 0, "09:30", "10:45"),
                createLecture("영어", 1, "11:00", "12:15"),
            )

            given(memberService.getMemberIfExists(1L)).willReturn(member)
            given(everytimeApi.fetchTimetable("testId")).willReturn(lectures)
            given(routeService.calculateRouteTime(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyNonNull(), anyNonNull(),
            )).willReturn(25)
            given(scheduleService.createEverytimeSchedule(
                anyNonNull(), anyNonNull(), anyNonNull(), anyNonNull(),
                anyNonNull(), anyNonNull(), anyInt(), anyNonNull(),
            )).willReturn(Schedule())

            // when
            facade.createSchedulesFromEverytime(
                1L, "https://everytime.kr/@testId",
                127.0, 37.0, 126.9, 37.5, TransportType.PUBLIC_TRANSPORT,
            )

            // then
            Mockito.verify(routeService, Mockito.times(1))
                .calculateRouteTime(
                    anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyNonNull(), anyNonNull(),
                )
        }
    }

    private fun createLecture(
        name: String, day: Int, startTime: String, endTime: String,
    ): EverytimeLecture = EverytimeLecture(
        name = name,
        day = day,
        startTime = LocalTime.parse(startTime),
        endTime = LocalTime.parse(endTime),
        place = "",
    )
}
