package com.dh.ondot.member.api.swagger;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.api.request.UpdateHomeAddressRequest;
import com.dh.ondot.member.api.request.UpdateMapProviderRequest;
import com.dh.ondot.member.api.request.WithdrawalRequest;
import com.dh.ondot.member.api.response.*;
import com.dh.ondot.core.domain.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/*──────────────────────────────────────────────────────────────
 * Member Swagger
 *──────────────────────────────────────────────────────────────*/
@Tag(
        name = "Member API",
        description = """
        <b>AccessToken (Authorization: Bearer JWT)</b>은 필수입니다.<br><br>
        <b>🏠 AddressType ENUM</b> : <code>HOME</code><br>
        <b>🗺 MapProvider ENUM</b> : <code>NAVER</code>, <code>KAKAO</code><br><br>
        <b>📢 주요 ErrorCode</b><br>
        • <code>NOT_FOUND_MEMBER</code> : 회원 미존재<br>
        • <code>NOT_FOUND_ADDRESS</code> : HOME 주소 미존재<br>
        • <code>FIELD_ERROR</code> / <code>URL_PARAMETER_ERROR</code> : 입력 검증 오류<br>
        • <code>UNSUPPORTED_MAP_PROVIDER</code> : 지원하지 않는 MapProvider 값<br>
        """
)
@RequestMapping("/members")
public interface MemberSwagger {

    /*──────────────────────────────────────────────────────
     * 회원 탈퇴
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "회원 탈퇴",
            description = """
            회원 탈퇴 요청을 처리합니다. 탈퇴 사유 ID는 필수이며, 기타 사유는 300자 이내로 입력할 수 있습니다.
        
            사유 목록(withdrawalReasonId):
            - ID 1: 지각 방지에 효과를 못 느꼈어요.
            - ID 2: 일정 등록이나 사용이 번거로웠어요.
            - ID 3: 알림이 너무 많거나 타이밍이 맞지 않았어요.
            - ID 4: 제 생활에 딱히 쓸 일이 없었어요.
            - ID 5: 기타
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "탈퇴 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WithdrawalRequest.class),
                            examples = @ExampleObject(value = """
            {
              "withdrawalReasonId": 5,
              "customReason": "서비스가 기대에 미치지 못했어요."
            }
            """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "탈퇴 완료"),
                    @ApiResponse(responseCode = "404",
                            description = "NOT_FOUND_MEMBER",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "errorCode": "NOT_FOUND_MEMBER",
                          "message": "회원을 찾을 수 없습니다. MemberId : 42"
                        }"""
                                    )))
            }
    )
    @PostMapping("/deactivate")
    void deactivateMember(
            @RequestAttribute("memberId") Long memberId,
            WithdrawalRequest request
    );

    /*──────────────────────────────────────────────────────
     * HOME 주소 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "회원 HOME 주소 조회",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = HomeAddressResponse.class),
                                    examples = @ExampleObject(
                                            name = "success",
                                            value = """
                        {
                          "roadAddress": "서울특별시 강남구 테헤란로 123",
                          "longitude": 127.0276,
                          "latitude": 37.4979
                        }"""
                                    ))),
                    @ApiResponse(responseCode = "404",
                            description = "주소 또는 회원 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "addressNotFound",
                                                    summary = "NOT_FOUND_ADDRESS",
                                                    value = """
                            {
                              "errorCode": "NOT_FOUND_ADDRESS",
                              "message": "회원이 저장한 주소를 찾을 수 없습니다. MemberId : 42"
                            }"""
                                            ),
                                            @ExampleObject(
                                                    name = "memberNotFound",
                                                    summary = "NOT_FOUND_MEMBER",
                                                    value = """
                            {
                              "errorCode": "NOT_FOUND_MEMBER",
                              "message": "회원을 찾을 수 없습니다. MemberId : 42"
                            }"""
                                            )
                                    }))
            }
    )
    @GetMapping("/home-address")
    HomeAddressResponse getHomeAddress(@RequestAttribute("memberId") Long memberId);

    /*──────────────────────────────────────────────────────
     * 온보딩 완료
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "온보딩(첫 설정) 완료",
            description = """
            사용자 온보딩을 완료합니다. <br>
            <b>🔔 Alarm ENUM</b><br>
            • <code>AlarmMode</code>: SILENT, VIBRATE, SOUND<br>
            • <code>SnoozeInterval</code>: 1, 3, 5, 10, 30, 60 (분)<br>
            • <code>SnoozeCount</code>: -1(INFINITE), 1, 3, 5, 10 (회)<br>
            • <code>SoundCategory</code>: <i>BRIGHT_ENERGY, FAST_INTENSE</i><br>
            • <code>RingTone</code>: <i>
              DANCING_IN_THE_STARDUST, IN_THE_CITY_LIGHTS_MIST, FRACTURED_LOVE,<br>
              CHASING_LIGHTS, ASHES_OF_US, HEATING_SUN, NO_COPYRIGHT_MUSIC,<br>
              MEDAL, EXCITING_SPORTS_COMPETITIONS, POSITIVE_WAY,<br>
              ENERGETIC_HAPPY_UPBEAT_ROCK_MUSIC, ENERGY_CATCHER
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = OnboardingRequest.class),
                            examples = @ExampleObject(name = "onboardingRequest",
                                    value = """
                    {
                      "preparationTime": 30,
                      "roadAddress": "서울특별시 강남구 테헤란로 123",
                      "longitude": 127.0276,
                      "latitude": 37.4979,
                      "alarmMode": "VIBRATE",
                      "isSnoozeEnabled": true,
                      "snoozeInterval": 5,
                      "snoozeCount": 3,
                      "soundCategory": "BRIGHT_ENERGY",
                      "ringTone": "FRACTURED_LOVE",
                      "volume": 0.2,
                      "questions": [
                        { "questionId": 1, "answerId": 3 },
                        { "questionId": 2, "answerId": 5 }
                      ]
                    }"""
                            ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "온보딩 성공",
                            content = @Content(schema = @Schema(implementation = OnboardingResponse.class),
                                    examples = @ExampleObject(
                                            name = "success",
                                            value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "updatedAt": "2025-05-10T12:34:56"
                        }"""
                                    ))),
                    @ApiResponse(responseCode = "400",
                            description = "검증 오류 / 지원하지 않는 값",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "fieldError",
                                                    summary = "FIELD_ERROR",
                                                    value = """
                            {
                              "errorCode": "FIELD_ERROR",
                              "message": "입력이 잘못되었습니다.",
                              "fieldErrors": [
                                { "field": "preparationTime", "rejectedValue": -1, "reason": "must be between 1 and 240" }
                              ]
                            }"""
                                            ),
                                            @ExampleObject(
                                                    name = "unsupportedMapProvider",
                                                    summary = "UNSUPPORTED_MAP_PROVIDER",
                                                    value = """
                            {
                              "errorCode": "UNSUPPORTED_MAP_PROVIDER",
                              "message": "지원하지 않는 지도 제공자입니다. MapProvider : ABC"
                            }"""
                                            )
                                    })),
                    @ApiResponse(responseCode = "404",
                            description = "질문/답변/회원 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            name = "questionNotFound",
                                            summary = "NOT_FOUND_QUESTION",
                                            value = """
                            {
                              "errorCode": "NOT_FOUND_QUESTION",
                              "message": "질문을 찾을 수 없습니다. QuestionId : 99"
                            }"""
                                    )))
            }
    )
    @PostMapping("/onboarding")
    OnboardingResponse onboarding(@RequestAttribute("memberId") Long memberId,
                                  @RequestBody OnboardingRequest request);

    /*──────────────────────────────────────────────────────
     * MapProvider 변경
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "지도 공급자(MapProvider) 변경",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateMapProviderRequest.class),
                            examples = @ExampleObject(value = "{ \"mapProvider\": \"KAKAO\" }"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "변경 성공",
                            content = @Content(schema = @Schema(implementation = UpdateMapProviderResponse.class),
                                    examples = @ExampleObject(
                                            name = "success",
                                            value = """
                        {
                          "memberId": 42,
                          "updatedAt": "2025-05-11T09:00:00"
                        }"""
                                    ))),
                    @ApiResponse(responseCode = "400",
                            description = "UNSUPPORTED_MAP_PROVIDER",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "errorCode": "UNSUPPORTED_MAP_PROVIDER",
                          "message": "지원하지 않는 지도 제공자입니다. MapProvider : ABC"
                        }"""
                                    ))),
                    @ApiResponse(responseCode = "404",
                            description = "NOT_FOUND_MEMBER",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "errorCode": "NOT_FOUND_MEMBER",
                          "message": "회원을 찾을 수 없습니다. MemberId : 42"
                        }"""
                                    )))
            }
    )
    @PatchMapping("/map-provider")
    UpdateMapProviderResponse updateMapProvider(@RequestAttribute("memberId") Long memberId,
                                                @RequestBody UpdateMapProviderRequest request);

    /*──────────────────────────────────────────────────────
     * HOME 주소 수정
     *──────────────────────────────────────────────────────*/
    @Operation(
            summary = "HOME 주소 수정",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateHomeAddressRequest.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "roadAddress": "서울특별시 강남구 테헤란로 456",
                      "longitude": 127.0301,
                      "latitude": 37.4982
                    }"""
                            ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = UpdateHomeAddressResponse.class),
                                    examples = @ExampleObject(
                                            name = "success",
                                            value = """
                        {
                          "roadAddress": "서울특별시 강남구 테헤란로 456",
                          "longitude": 127.0301,
                          "latitude": 37.4982
                        }"""
                                    ))),
                    @ApiResponse(responseCode = "404",
                            description = "NOT_FOUND_ADDRESS | NOT_FOUND_MEMBER",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "errorCode": "NOT_FOUND_ADDRESS",
                          "message": "회원이 저장한 주소를 찾을 수 없습니다. MemberId : 42"
                        }"""
                                    )))
            }
    )
    @PatchMapping("/home-address")
    UpdateHomeAddressResponse updateHomeAddress(@RequestAttribute("memberId") Long memberId,
                                                @RequestBody UpdateHomeAddressRequest request);
}
