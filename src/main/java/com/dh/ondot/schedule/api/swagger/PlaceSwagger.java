package com.dh.ondot.schedule.api.swagger;

import com.dh.ondot.core.domain.ErrorResponse;
import com.dh.ondot.schedule.api.request.PlaceHistorySaveRequest;
import com.dh.ondot.schedule.api.response.PlaceHistoryResponse;
import com.dh.ondot.schedule.api.response.PlaceSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Place API",
        description = """
        <p><b>장소(Place) 검색·최근 기록</b> 기능을 제공합니다.</p>
        """
)
@RequestMapping("/places")
public interface PlaceSwagger {

    /* 1) 장소 키워드 검색 */
    @Operation(
            summary = "장소 키워드 검색",
            description = """
            입력 키워드로 네이버 플레이스·도로명 주소를 통합 검색합니다.<br>
            • query 파라미터 하나만 받으며, 최소 1글자 이상이어야 합니다.
            """,
            parameters = @Parameter(
                    name = "query",
                    description = "검색 키워드(1자 이상)",
                    in = ParameterIn.QUERY,
                    required = true,
                    example = "강남역"
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검색 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = PlaceSearchResponse.class)),
                                    examples = @ExampleObject(
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
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "query 파라미터 누락 / 공백",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            name = "InvalidQuery",
                                            value = """
                            {
                              "errorCode": "URL_PARAMETER_ERROR",
                              "message": "query 파라미터는 필수입니다."
                            }"""
                                    )
                            )
                    )
            }
    )
    @GetMapping("/search")
    List<PlaceSearchResponse> searchPlaces(
            @NotBlank(message = "query 파라미터는 필수입니다.")
            @RequestParam("query") String query
    );

    /* 2) 최근 검색 기록 저장 */
    @Operation(
            summary = "최근 검색 기록 저장",
            description = """
            클라이언트가 사용자가 선택한 장소를 전달하면 <b>회원별 검색 기록</b>으로 남깁니다.
            <ul>
              <li>title이 <code>null</code>이면 <code>roadAddress</code> 값을 title 자리에 복사하여 저장합니다.</li>
              <li>회원당 최대 10건만 보관, 30일 경과 데이터는 제거합니다.</li>
            </ul>
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlaceHistorySaveRequest.class),
                            examples = @ExampleObject(
                                    name = "SaveHistoryRequest",
                                    value = """
                        {
                          "title": "가천대학교 글로벌캠퍼스글로벌센터",
                          "roadAddress": "경기도 성남시 수정구 성남대로 1342",
                          "longitude": 127.12728,
                          "latitude": 37.4519485
                        }"""
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "저장 성공 (본문 없음)"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "필드 검증 실패",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
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
                                            @ExampleObject(
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
                                    }
                            )
                    )
            }
    )
    @PostMapping("/history")
    void saveHistory(
            @Parameter(hidden = true) @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody PlaceHistorySaveRequest request
    );

    /* 3) 최근 검색 기록 조회 */
    @Operation(
            summary = "최근 검색 기록 조회",
            description = """
            회원별로 저장된 <b>최근 10건</b>의 장소 기록을 최신순으로 반환합니다.
            <br><br><b>searchedAt</b>은 ISO‑8601(예: <code>2025‑04‑15T10:30:45</code>) 문자열입니다.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = PlaceHistoryResponse.class)),
                                    examples = @ExampleObject(
                                            name = "HistorySuccess",
                                            value = """
                            [
                              {
                                "title": "가천대학교 글로벌캠퍼스글로벌센터",
                                "longitude": 127.12728,
                                "latitude": 37.4519485,
                                "searchedAt": "2025-04-15T09:20:31"
                              },
                              {
                                "title": "흥덕1로 79번길 37",
                                "longitude": 126.9763,
                                "latitude": 37.4847,
                                "searchedAt": "2025-04-14T18:03:10"
                              }
                            ]"""
                                    )
                            )
                    )
            }
    )
    @GetMapping("/history")
    List<PlaceHistoryResponse> history(
            @Parameter(hidden = true) @RequestAttribute("memberId") Long memberId
    );
}