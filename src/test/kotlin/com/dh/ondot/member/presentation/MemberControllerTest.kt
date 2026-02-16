package com.dh.ondot.member.presentation

import com.dh.ondot.core.TokenInterceptor
import com.dh.ondot.core.exception.GlobalExceptionHandler
import com.dh.ondot.member.application.MemberFacade
import com.dh.ondot.member.core.OauthProviderConverter
import com.dh.ondot.member.domain.Address
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.MapProvider
import com.dh.ondot.member.presentation.response.OnboardingResponse
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.LocalDateTime

@WebMvcTest(MemberController::class)
@Import(OauthProviderConverter::class, GlobalExceptionHandler::class)
@DisplayName("MemberController 테스트")
class MemberControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var memberFacade: MemberFacade

    @MockitoBean
    private lateinit var tokenInterceptor: TokenInterceptor

    private val memberId = 1L

    @BeforeEach
    fun setUp() {
        whenever(tokenInterceptor.preHandle(any(), any(), any())).thenReturn(true)
    }

    @Nested
    @DisplayName("DELETE /members")
    inner class DeleteMember {

        @Test
        @DisplayName("정상 요청 시 204를 반환한다")
        fun success_204() {
            val body = mapOf("withdrawalReasonId" to 1, "customReason" to "테스트")

            mockMvc.perform(
                delete("/members")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isNoContent)

            verify(memberFacade).deleteMember(memberId, 1L, "테스트")
        }

        @Test
        @DisplayName("customReason이 300자 초과 시 400을 반환한다")
        fun customReasonExceeds300_400() {
            val body = mapOf("withdrawalReasonId" to 1, "customReason" to "a".repeat(301))

            mockMvc.perform(
                delete("/members")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /members/onboarding")
    inner class Onboarding {

        @Test
        @DisplayName("정상 요청 시 200과 OnboardingResponse를 반환한다")
        fun success_200() {
            val response = OnboardingResponse("access", "refresh", LocalDateTime.of(2025, 1, 1, 0, 0))
            whenever(memberFacade.onboarding(any(), any(), any())).thenReturn(response)

            val body = mapOf(
                "preparationTime" to 30,
                "roadAddress" to "서울시 강남구",
                "longitude" to 127.0,
                "latitude" to 37.0,
                "alarmMode" to "VIBRATE",
                "isSnoozeEnabled" to true,
                "snoozeInterval" to 5,
                "snoozeCount" to 3,
                "soundCategory" to "DEFAULT",
                "ringTone" to "DEFAULT",
                "volume" to 0.5,
                "questions" to listOf(mapOf("questionId" to 1, "answerId" to 1))
            )

            mockMvc.perform(
                post("/members/onboarding")
                    .requestAttr("memberId", memberId)
                    .header("X-Mobile-Type", "ANDROID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
        }

        @Test
        @DisplayName("preparationTime 범위 초과 시 400을 반환한다")
        fun prepTimeExceedsMax_400() {
            val body = mapOf(
                "preparationTime" to 601,
                "roadAddress" to "서울시 강남구",
                "longitude" to 127.0,
                "latitude" to 37.0,
                "alarmMode" to "VIBRATE",
                "isSnoozeEnabled" to true,
                "snoozeInterval" to 5,
                "snoozeCount" to 3,
                "soundCategory" to "DEFAULT",
                "ringTone" to "DEFAULT",
                "volume" to 0.5,
                "questions" to listOf(mapOf("questionId" to 1, "answerId" to 1))
            )

            mockMvc.perform(
                post("/members/onboarding")
                    .requestAttr("memberId", memberId)
                    .header("X-Mobile-Type", "ANDROID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("roadAddress 빈값 시 400을 반환한다")
        fun emptyRoadAddress_400() {
            val body = mapOf(
                "preparationTime" to 30,
                "roadAddress" to "",
                "longitude" to 127.0,
                "latitude" to 37.0,
                "alarmMode" to "VIBRATE",
                "isSnoozeEnabled" to true,
                "snoozeInterval" to 5,
                "snoozeCount" to 3,
                "soundCategory" to "DEFAULT",
                "ringTone" to "DEFAULT",
                "volume" to 0.5,
                "questions" to listOf(mapOf("questionId" to 1, "answerId" to 1))
            )

            mockMvc.perform(
                post("/members/onboarding")
                    .requestAttr("memberId", memberId)
                    .header("X-Mobile-Type", "ANDROID")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /members/home-address")
    inner class GetHomeAddress {

        @Test
        @DisplayName("정상 요청 시 200과 HomeAddressResponse를 반환한다")
        fun success_200() {
            val address: Address = mock {
                on { roadAddress } doReturn "서울시 강남구"
                on { longitude } doReturn 127.0
                on { latitude } doReturn 37.0
            }
            given(memberFacade.getHomeAddress(memberId)).willReturn(address)

            mockMvc.perform(
                get("/members/home-address").requestAttr("memberId", memberId)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.roadAddress").value("서울시 강남구"))
                .andExpect(jsonPath("$.longitude").value(127.0))
                .andExpect(jsonPath("$.latitude").value(37.0))
        }
    }

    @Nested
    @DisplayName("GET /members/map-provider")
    inner class GetMapProvider {

        @Test
        @DisplayName("정상 요청 시 200과 MapProviderResponse를 반환한다")
        fun success_200() {
            val member: Member = mock {
                on { mapProvider } doReturn MapProvider.KAKAO
                on { updatedAt } doReturn Instant.parse("2025-01-01T00:00:00Z")
            }
            given(memberFacade.getMember(memberId)).willReturn(member)

            mockMvc.perform(
                get("/members/map-provider").requestAttr("memberId", memberId)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.mapProvider").value("KAKAO"))
        }
    }

    @Nested
    @DisplayName("PATCH /members/map-provider")
    inner class UpdateMapProvider {

        @Test
        @DisplayName("정상 요청 시 200과 MapProviderResponse를 반환한다")
        fun success_200() {
            val member: Member = mock {
                on { mapProvider } doReturn MapProvider.NAVER
                on { updatedAt } doReturn Instant.parse("2025-01-01T00:00:00Z")
            }
            whenever(memberFacade.updateMapProvider(any(), any())).thenReturn(member)

            mockMvc.perform(
                patch("/members/map-provider")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"mapProvider":"NAVER"}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.mapProvider").value("NAVER"))
        }

        @Test
        @DisplayName("mapProvider 빈값 시 400을 반환한다")
        fun emptyMapProvider_400() {
            mockMvc.perform(
                patch("/members/map-provider")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"mapProvider":""}""")
            ).andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("PATCH /members/home-address")
    inner class UpdateHomeAddress {

        @Test
        @DisplayName("정상 요청 시 200과 UpdateHomeAddressResponse를 반환한다")
        fun success_200() {
            val address: Address = mock {
                on { roadAddress } doReturn "서울시 서초구"
                on { longitude } doReturn 127.1
                on { latitude } doReturn 37.1
            }
            whenever(memberFacade.updateHomeAddress(any(), any(), any(), any())).thenReturn(address)

            mockMvc.perform(
                patch("/members/home-address")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"roadAddress":"서울시 서초구","longitude":127.1,"latitude":37.1}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.roadAddress").value("서울시 서초구"))
        }

        @Test
        @DisplayName("longitude 범위 초과 시 400을 반환한다")
        fun longitudeExceedsMax_400() {
            mockMvc.perform(
                patch("/members/home-address")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"roadAddress":"서울","longitude":181.0,"latitude":37.0}""")
            ).andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /members/preparation-time")
    inner class GetPreparationTime {

        @Test
        @DisplayName("정상 요청 시 200과 PreparationTimeResponse를 반환한다")
        fun success_200() {
            val member: Member = mock {
                on { preparationTime } doReturn 30
                on { updatedAt } doReturn Instant.parse("2025-01-01T00:00:00Z")
            }
            given(memberFacade.getMember(memberId)).willReturn(member)

            mockMvc.perform(
                get("/members/preparation-time").requestAttr("memberId", memberId)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.preparationTime").value(30))
        }
    }

    @Nested
    @DisplayName("PATCH /members/preparation-time")
    inner class UpdatePreparationTime {

        @Test
        @DisplayName("정상 요청 시 200과 PreparationTimeResponse를 반환한다")
        fun success_200() {
            val member: Member = mock {
                on { preparationTime } doReturn 45
                on { updatedAt } doReturn Instant.parse("2025-01-01T00:00:00Z")
            }
            whenever(memberFacade.updatePreparationTime(any(), any())).thenReturn(member)

            mockMvc.perform(
                patch("/members/preparation-time")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"preparationTime":45}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.preparationTime").value(45))
        }

        @Test
        @DisplayName("preparationTime 범위 초과 시 400을 반환한다")
        fun exceedsMax_400() {
            mockMvc.perform(
                patch("/members/preparation-time")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"preparationTime":601}""")
            ).andExpect(status().isBadRequest)
        }
    }
}
