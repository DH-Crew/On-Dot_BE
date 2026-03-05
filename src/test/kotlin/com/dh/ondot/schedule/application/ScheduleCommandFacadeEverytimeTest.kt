package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.application.command.CreateEverytimeScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
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
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import java.time.DayOfWeek
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
        @DisplayName("유효한 에브리타임 URL로 요일별 시간표를 반환한다")
        fun validateEverytimeUrl_ValidUrl_ReturnsTimetable() {
            // given
            val url = "https://everytime.kr/@ip9ktZ3A7H35H6P7Z1Wr"
            val lectures = listOf(
                createLecture("수학", 0, "09:30", "10:45"),
                createLecture("영어", 0, "11:00", "12:15"),
                createLecture("물리", 1, "09:30", "10:45"),
            )
            given(everytimeApi.fetchTimetable("ip9ktZ3A7H35H6P7Z1Wr"))
                .willReturn(lectures)

            // when
            val result = facade.validateEverytimeUrl(url)

            // then
            assertThat(result).containsKeys(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
            assertThat(result[DayOfWeek.MONDAY]).hasSize(2)
            assertThat(result[DayOfWeek.MONDAY]!![0].startTime).isEqualTo(LocalTime.of(9, 30))
            assertThat(result[DayOfWeek.MONDAY]!![1].startTime).isEqualTo(LocalTime.of(11, 0))
            assertThat(result[DayOfWeek.TUESDAY]).hasSize(1)
        }

        @Test
        @DisplayName("빈 시간표일 경우 EverytimeEmptyTimetableException이 발생한다")
        fun validateEverytimeUrl_EmptyTimetable_ThrowsException() {
            // given
            val url = "https://everytime.kr/@emptyId"
            given(everytimeApi.fetchTimetable("emptyId")).willReturn(emptyList())

            // when & then
            assertThatThrownBy { facade.validateEverytimeUrl(url) }
                .isInstanceOf(EverytimeEmptyTimetableException::class.java)
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
        @DisplayName("동일 시간대를 선택한 요일들은 하나의 반복 스케줄로 묶인다")
        fun createSchedules_SameTimeDays_GroupedIntoOneSchedule() {
            // given
            val member = MemberFixture.defaultMember()
            val selectedLectures = listOf(
                CreateEverytimeScheduleCommand.SelectedLecture(DayOfWeek.MONDAY, LocalTime.of(9, 30)),
                CreateEverytimeScheduleCommand.SelectedLecture(DayOfWeek.WEDNESDAY, LocalTime.of(9, 30)),
                CreateEverytimeScheduleCommand.SelectedLecture(DayOfWeek.TUESDAY, LocalTime.of(11, 0)),
                CreateEverytimeScheduleCommand.SelectedLecture(DayOfWeek.THURSDAY, LocalTime.of(11, 0)),
            )

            given(memberService.getMemberIfExists(1L)).willReturn(member)
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
                1L, createCommand(selectedLectures),
            )

            // then
            assertThat(result).hasSize(2)
        }

        @Test
        @DisplayName("대중교통 경로 계산은 1회만 호출된다")
        fun createSchedules_PublicTransport_CalculatesRouteOnce() {
            // given
            val member = MemberFixture.defaultMember()
            val selectedLectures = listOf(
                CreateEverytimeScheduleCommand.SelectedLecture(DayOfWeek.MONDAY, LocalTime.of(9, 30)),
                CreateEverytimeScheduleCommand.SelectedLecture(DayOfWeek.TUESDAY, LocalTime.of(11, 0)),
            )

            given(memberService.getMemberIfExists(1L)).willReturn(member)
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
                1L, createCommand(selectedLectures),
            )

            // then
            Mockito.verify(routeService, Mockito.times(1))
                .calculateRouteTime(
                    anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyNonNull(), anyNonNull(),
                )
        }
    }

    private fun createCommand(
        selectedLectures: List<CreateEverytimeScheduleCommand.SelectedLecture>,
        transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
    ): CreateEverytimeScheduleCommand = CreateEverytimeScheduleCommand(
        selectedLectures = selectedLectures,
        departurePlace = CreateScheduleCommand.PlaceInfo("집", "서울시 강남구", 127.0, 37.0),
        arrivalPlace = CreateScheduleCommand.PlaceInfo("학교", "서울시 서초구", 126.9, 37.5),
        transportType = transportType,
    )

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
