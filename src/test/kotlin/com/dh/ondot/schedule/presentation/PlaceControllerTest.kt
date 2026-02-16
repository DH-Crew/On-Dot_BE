package com.dh.ondot.schedule.presentation

import com.dh.ondot.core.TokenInterceptor
import com.dh.ondot.core.exception.GlobalExceptionHandler
import com.dh.ondot.schedule.application.PlaceFacade
import com.dh.ondot.schedule.application.dto.PlaceSearchResult
import com.dh.ondot.schedule.domain.PlaceHistory
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.LocalDateTime

@WebMvcTest(PlaceController::class)
@Import(GlobalExceptionHandler::class)
@DisplayName("PlaceController 테스트")
class PlaceControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var placeFacade: PlaceFacade

    @MockitoBean
    private lateinit var tokenInterceptor: TokenInterceptor

    private val memberId = 1L

    @BeforeEach
    fun setUp() {
        whenever(tokenInterceptor.preHandle(any(), any(), any())).thenReturn(true)
    }

    // ═══════════════════════════════════════════════════
    // GET /places/search — searchPlaces
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /places/search")
    inner class SearchPlaces {

        @Test
        @DisplayName("정상 요청 시 200과 PlaceSearchResponse 리스트를 반환한다")
        fun success_200() {
            val results = listOf(
                PlaceSearchResult("서울역", "서울특별시 용산구 한강대로 405", 126.9726, 37.5547),
                PlaceSearchResult("서울역 버스환승센터", "서울특별시 용산구 한강대로 392", 126.9718, 37.5540),
            )
            whenever(placeFacade.searchPlaces("서울역")).thenReturn(results)

            mockMvc.perform(
                get("/places/search")
                    .param("query", "서울역")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].title").value("서울역"))
                .andExpect(jsonPath("$[0].roadAddress").value("서울특별시 용산구 한강대로 405"))
                .andExpect(jsonPath("$[0].longitude").value(126.9726))
                .andExpect(jsonPath("$[0].latitude").value(37.5547))
                .andExpect(jsonPath("$[1].title").value("서울역 버스환승센터"))
        }

        @Test
        @DisplayName("query가 빈값이면 400을 반환한다")
        fun emptyQuery_400() {
            mockMvc.perform(
                get("/places/search")
                    .param("query", "")
            ).andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("query 파라미터가 없으면 400을 반환한다")
        fun missingQuery_400() {
            mockMvc.perform(
                get("/places/search")
            ).andExpect(status().isBadRequest)
        }
    }

    // ═══════════════════════════════════════════════════
    // POST /places/history — saveHistory
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /places/history")
    inner class SaveHistory {

        @Test
        @DisplayName("정상 요청 시 201을 반환한다")
        fun success_201() {
            val body = mapOf(
                "title" to "서울역",
                "roadAddress" to "서울특별시 용산구 한강대로 405",
                "longitude" to 126.9726,
                "latitude" to 37.5547,
            )

            mockMvc.perform(
                post("/places/history")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isCreated)

            verify(placeFacade).saveHistory(any(), any())
        }

        @Test
        @DisplayName("roadAddress가 빈값이면 400을 반환한다")
        fun emptyRoadAddress_400() {
            val body = mapOf(
                "title" to "서울역",
                "roadAddress" to "",
                "longitude" to 126.9726,
                "latitude" to 37.5547,
            )

            mockMvc.perform(
                post("/places/history")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("longitude가 범위를 초과하면 400을 반환한다")
        fun coordinateExceedsRange_400() {
            val body = mapOf(
                "title" to "서울역",
                "roadAddress" to "서울특별시 용산구 한강대로 405",
                "longitude" to 181.0,
                "latitude" to 37.5547,
            )

            mockMvc.perform(
                post("/places/history")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }
    }

    // ═══════════════════════════════════════════════════
    // GET /places/history — history
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /places/history")
    inner class GetHistory {

        @Test
        @DisplayName("정상 요청 시 200과 PlaceHistoryResponse 리스트를 반환한다")
        fun success_200() {
            val histories = listOf(
                PlaceHistory(
                    memberId = 1L,
                    title = "서울역",
                    roadAddress = "서울특별시 용산구 한강대로 405",
                    longitude = 126.9726,
                    latitude = 37.5547,
                    searchedAt = Instant.parse("2025-01-01T00:00:00Z"),
                ),
            )
            whenever(placeFacade.getHistory(1L)).thenReturn(histories)

            mockMvc.perform(
                get("/places/history")
                    .requestAttr("memberId", memberId)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].title").value("서울역"))
                .andExpect(jsonPath("$[0].roadAddress").value("서울특별시 용산구 한강대로 405"))
                .andExpect(jsonPath("$[0].longitude").value(126.9726))
                .andExpect(jsonPath("$[0].latitude").value(37.5547))
                .andExpect(jsonPath("$[0].searchedAt").exists())
        }
    }

    // ═══════════════════════════════════════════════════
    // DELETE /places/history — deleteHistory
    // ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /places/history")
    inner class DeleteHistory {

        @Test
        @DisplayName("정상 요청 시 204를 반환한다")
        fun success_204() {
            val body = mapOf(
                "searchedAt" to "2025-01-01T10:00:00",
            )

            mockMvc.perform(
                delete("/places/history")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isNoContent)

            verify(placeFacade).deleteHistory(any(), any())
        }

        @Test
        @DisplayName("searchedAt이 없으면 400을 반환한다")
        fun missingSearchedAt_400() {
            val body = mapOf<String, Any>()

            mockMvc.perform(
                delete("/places/history")
                    .requestAttr("memberId", memberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            ).andExpect(status().isBadRequest)
        }
    }
}
