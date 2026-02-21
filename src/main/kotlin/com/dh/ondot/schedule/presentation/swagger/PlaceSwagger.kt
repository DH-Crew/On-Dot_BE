package com.dh.ondot.schedule.presentation.swagger

import com.dh.ondot.core.ErrorResponse
import com.dh.ondot.schedule.presentation.request.PlaceHistoryDeleteRequest
import com.dh.ondot.schedule.presentation.request.PlaceHistorySaveRequest
import com.dh.ondot.schedule.presentation.response.PlaceHistoryResponse
import com.dh.ondot.schedule.presentation.response.PlaceSearchResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(
    name = "Place API",
    description = """
        **장소(Place)** 관련 API입니다.
        """
)
@RequestMapping("/places")
interface PlaceSwagger {

    /* 1) 장소 키워드 검색 */
    @Operation(
        summary = "장소 키워드 검색",
        description = """
            입력 키워드로 네이버 플레이스·도로명 주소를 통합 검색합니다.
            - query 파라미터 하나만 받으며, 최소 1글자 이상이어야 합니다.
            """,
        parameters = [Parameter(
            name = "query",
            description = "검색 키워드(1자 이상)",
            `in` = ParameterIn.QUERY,
            required = true,
            example = "강남역"
        )],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "검색 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = ArraySchema(schema = Schema(implementation = PlaceSearchResponse::class)),
                    examples = [ExampleObject(
                        name = "SearchSuccess",
                        value = """
                            [
                              {
                                "title": "강남역 2호선",
                                "roadAddress": "서울특별시 강남구 강남대로 396",
                                "longitude": 127.0276,
                                "latitude": 37.4979
                              },
                              {
                                "title": "스타벅스 강남역점",
                                "roadAddress": "서울특별시 강남구 테헤란로 10",
                                "longitude": 127.0284,
                                "latitude": 37.4975
                              }
                            ]"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "query 파라미터 누락 / 공백",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "InvalidQuery",
                        value = """
                            {
                              "errorCode": "URL_PARAMETER_ERROR",
                              "message": "query 파라미터는 필수입니다."
                            }"""
                    )]
                )]
            )
        ]
    )
    @GetMapping("/search")
    fun searchPlaces(
        @NotBlank(message = "query 파라미터는 필수입니다.")
        @RequestParam("query") query: String,
    ): List<PlaceSearchResponse>

    /* 2) 최근 검색 기록 저장 */
    @Operation(
        summary = "최근 검색 기록 저장",
        description = """
            클라이언트가 사용자가 선택한 장소를 전달하면 **회원별 검색 기록**으로 남깁니다.
            - title이 `null`이면 `roadAddress` 값을 title 자리에 복사하여 저장합니다.
            - 회원당 최대 10건만 보관, 30일 경과 데이터는 제거합니다.
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = PlaceHistorySaveRequest::class),
                examples = [ExampleObject(
                    name = "SaveHistoryRequest",
                    value = """
                        {
                          "title": "가천대학교 글로벌캠퍼스글로벌센터",
                          "roadAddress": "경기도 성남시 수정구 성남대로 1342",
                          "longitude": 127.12728,
                          "latitude": 37.4519485
                        }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "저장 성공 (본문 없음)"),
            ApiResponse(
                responseCode = "400",
                description = "필드 검증 실패",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "Blank roadAddress",
                            summary = "도로명 주소가 빈 문자열",
                            value = """
                    {
                      "errorCode": "FIELD_ERROR",
                      "message": "입력이 잘못되었습니다.",
                      "violationErrors": [
                        {
                          "path": "roadAddress",
                          "reason": "공백일 수 없습니다"
                        }
                      ]
                    }
                    """
                        ),
                        ExampleObject(
                            name = "Invalid latitude/longitude",
                            summary = "위도·경도 범위 벗어남",
                            value = """
                    {
                      "errorCode": "FIELD_ERROR",
                      "message": "입력이 잘못되었습니다.",
                      "violationErrors": [
                        {
                          "path": "latitude",
                          "reason": "-91.0은 허용된 최소값 -90.0보다 작습니다"
                        },
                        {
                          "path": "longitude",
                          "reason": "200.0은 허용된 최대값 180.0보다 큽니다"
                        }
                      ]
                    }
                    """
                        )
                    ]
                )]
            )
        ]
    )
    @PostMapping("/history")
    fun saveHistory(
        @Parameter(hidden = true) @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: PlaceHistorySaveRequest,
    )

    /* 3) 최근 검색 기록 조회 */
    @Operation(
        summary = "최근 검색 기록 조회",
        description = """
            회원별로 저장된 **최근 10건**의 장소 기록을 최신순으로 반환합니다.

            **searchedAt**은 서울 시간대 기준 LocalDateTime 형식이며, 나노초 정밀도를 포함합니다 (예: `2025‑04‑15T18:20:31.123456789`).
            """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = ArraySchema(schema = Schema(implementation = PlaceHistoryResponse::class)),
                    examples = [ExampleObject(
                        name = "HistorySuccess",
                        value = """
                            [
                              {
                                "title": "가천대학교 글로벌캠퍼스글로벌센터",
                                "roadAddress": "경기도 성남시 수정구 성남대로 1342",
                                "longitude": 127.12728,
                                "latitude": 37.4519485,
                                "searchedAt": "2025-04-15T18:20:31.123456789"
                              },
                              {
                                "title": "흥덕1로 79번길 37",
                                "roadAddress": "서울특별시 용산구 흥덕1로 79번길 37",
                                "longitude": 126.9763,
                                "latitude": 37.4847,
                                "searchedAt": "2025-04-14T09:03:10.987654321"
                              }
                            ]"""
                    )]
                )]
            )
        ]
    )
    @GetMapping("/history")
    fun history(
        @Parameter(hidden = true) @RequestAttribute("memberId") memberId: Long,
    ): List<PlaceHistoryResponse>

    /* 4) 최근 검색 기록 삭제 */
    @Operation(
        summary = "최근 검색 기록 삭제",
        description = """
            회원별 검색 기록 중 **특정 항목을 삭제**합니다.
            - searchedAt 값은 조회 API에서 받은 값을 그대로 사용하세요 (예: `2025-04-15T18:20:31.123456789`).
            - 서울 시간대 기준 LocalDateTime 형식이며, 나노초 정밀도를 포함합니다.
            - 해당 시각의 기록이 존재하지 않아도 성공(204) 응답을 반환합니다.
            """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = PlaceHistoryDeleteRequest::class),
                examples = [ExampleObject(
                    name = "DeleteHistoryRequest",
                    value = """
                        {
                          "searchedAt": "2025-04-15T18:20:31.123456789"
                        }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(responseCode = "204", description = "삭제 성공 (본문 없음)"),
            ApiResponse(
                responseCode = "400",
                description = "필드 검증 실패",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "Blank searchedAt",
                            summary = "검색 시각이 빈 문자열",
                            value = """
                    {
                      "errorCode": "FIELD_ERROR",
                      "message": "입력이 잘못되었습니다.",
                      "violationErrors": [
                        {
                          "path": "searchedAt",
                          "reason": "검색 시각은 필수입니다."
                        }
                      ]
                    }
                    """
                        ),
                        ExampleObject(
                            name = "Invalid timestamp format",
                            summary = "타임스탬프 형식 오류",
                            value = """
                    {
                      "errorCode": "INVALID_INPUT_VALUE",
                      "message": "잘못된 타임스탬프 형식입니다."
                    }
                    """
                        )
                    ]
                )]
            )
        ]
    )
    @DeleteMapping("/history")
    fun deleteHistory(
        @Parameter(hidden = true) @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: PlaceHistoryDeleteRequest,
    )
}
