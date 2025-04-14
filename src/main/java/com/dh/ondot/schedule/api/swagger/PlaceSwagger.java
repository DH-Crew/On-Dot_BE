package com.dh.ondot.schedule.api.swagger;

import com.dh.ondot.core.domain.ErrorResponse;
import com.dh.ondot.schedule.api.response.PlaceSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Place API (Swagger 문서 전용)
 *
 * <p><b>인증</b> : 모든 요청은 <code>Authorization: Bearer JWT</code> 필요.<br>
 * TokenInterceptor가 검증 후 <code>memberId</code> 를 주입합니다.</p>
 *
 * <p><b>검색 로직</b> : POI API와 도로명 주소 API 결과를 라운드‑로빈으로 병합해
 * 최대 10~20개의 후보를 반환합니다.</p>
 */
@Tag(
        name = "Place API",
        description = """
        장소(Place) 검색 전용 엔드포인트입니다.<br>
        • query 파라미터 하나만 받으며, 최소 1글자 이상이어야 합니다.
        """
)
@RequestMapping("/places")
public interface PlaceSwagger {

    /*──────────────────────────────────────────────────────
     *  장소 검색
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "장소 키워드 검색",
            description = """
            입력 키워드로 네이버 플레이스·도로명 주소를 통합 검색합니다.<br>
            두 외부 API의 결과를 라운드‑로빈으로 병합해 응답합니다.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검색 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = PlaceSearchResponse.class)),
                                    examples = @ExampleObject(
                                            name = "placeSearchExample",
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
                            description = "query 파라미터 누락 또는 공백",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Missing Query Param",
                                                    summary = "쿼리 파라미터 없음",
                                                    value = """
                        {
                          "errorCode": "URL_PARAMETER_ERROR",
                          "message": "입력이 잘못되었습니다."
                        }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Blank Query Param",
                                                    summary = "빈 문자열 전달",
                                                    value = """
                        {
                          "errorCode": "URL_PARAMETER_ERROR",
                          "message": "query 파라미터는 필수입니다.",
                          "violationErrors": [
                            {
                              "path": "query",
                              "reason": "query 파라미터는 필수입니다."
                            }
                          ]
                        }"""
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/search")
    List<PlaceSearchResponse> searchPlaces(
            @Parameter(
                    description = "검색 키워드",
                    example = "강남역",
                    required = true
            )
            @RequestParam("query") String query
    );
}
