package com.dh.ondot.member.presentation.swagger

import com.dh.ondot.core.ErrorResponse
import com.dh.ondot.member.presentation.request.OnboardingRequest
import com.dh.ondot.member.presentation.request.UpdateHomeAddressRequest
import com.dh.ondot.member.presentation.request.UpdateMapProviderRequest
import com.dh.ondot.member.presentation.request.UpdatePreparationTimeRequest
import com.dh.ondot.member.presentation.request.WithdrawalRequest
import com.dh.ondot.member.presentation.response.HomeAddressResponse
import com.dh.ondot.member.presentation.response.MapProviderResponse
import com.dh.ondot.member.presentation.response.OnboardingResponse
import com.dh.ondot.member.presentation.response.PreparationTimeResponse
import com.dh.ondot.member.presentation.response.UpdateHomeAddressResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

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
                • <code>NOT_FOUND_HOME_ADDRESS</code> : HOME 주소 미존재<br>
                • <code>FIELD_ERROR</code> / <code>URL_PARAMETER_ERROR</code> : 입력 검증 오류<br>
                • <code>UNSUPPORTED_MAP_PROVIDER</code> : 지원하지 않는 MapProvider 값<br>
                """
)
@RequestMapping("/members")
interface MemberSwagger {

    /*──────────────────────────────────────────────────────
     * 회원 완전 삭제
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "회원 완전 삭제",
        description = """
                    회원과 관련된 모든 데이터를 완전히 삭제합니다. 이 작업은 되돌릴 수 없습니다.
                    삭제되는 데이터: 회원 정보, 주소, 선택사항, 일정, 알람

                    사유 목록(withdrawalReasonId):
                    - ID 1: 지각 방지에 효과를 못 느꼈어요.
                    - ID 2: 일정 등록이나 사용이 번거로웠어요.
                    - ID 3: 알림이 너무 많거나 타이밍이 맞지 않았어요.
                    - ID 4: 제 생활에 딱히 쓸 일이 없었어요.
                    - ID 5: 기타
                    """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "탈퇴 요청 정보",
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = WithdrawalRequest::class),
                examples = [ExampleObject(value = """
                                    {
                                      "withdrawalReasonId": 5,
                                      "customReason": "서비스가 기대에 미치지 못했어요."
                                    }
                                    """)]
            )]
        ),
        responses = [
            ApiResponse(responseCode = "204", description = "삭제 완료"),
            ApiResponse(
                responseCode = "404",
                description = "NOT_FOUND_MEMBER",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        value = """
                                                    {
                                                      "errorCode": "NOT_FOUND_MEMBER",
                                                      "message": "회원을 찾을 수 없습니다. MemberId : 42"
                                                    }"""
                    )]
                )]
            ),
        ]
    )
    @DeleteMapping
    fun deleteMember(
        @RequestAttribute("memberId") memberId: Long,
        request: WithdrawalRequest,
    )

    /*──────────────────────────────────────────────────────
     * HOME 주소 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "회원 HOME 주소 조회",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    schema = Schema(implementation = HomeAddressResponse::class),
                    examples = [ExampleObject(
                        name = "success",
                        value = """
                                                    {
                                                      "roadAddress": "서울특별시 강남구 테헤란로 123",
                                                      "longitude": 127.0276,
                                                      "latitude": 37.4979
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "주소 또는 회원 없음",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "addressNotFound",
                            summary = "NOT_FOUND_HOME_ADDRESS",
                            value = """
                                                            {
                                                              "errorCode": "NOT_FOUND_HOME_ADDRESS",
                                                              "message": "회원이 저장한 주소를 찾을 수 없습니다. MemberId : 42"
                                                            }"""
                        ),
                        ExampleObject(
                            name = "memberNotFound",
                            summary = "NOT_FOUND_MEMBER",
                            value = """
                                                            {
                                                              "errorCode": "NOT_FOUND_MEMBER",
                                                              "message": "회원을 찾을 수 없습니다. MemberId : 42"
                                                            }"""
                        ),
                    ]
                )]
            ),
        ]
    )
    @GetMapping("/home-address")
    fun getHomeAddress(@RequestAttribute("memberId") memberId: Long): HomeAddressResponse

    /*──────────────────────────────────────────────────────
     * MAP 제공자 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "회원 MAP 제공자 조회",
        description = """
                    로그인한 회원의 현재 MAP 제공자 정보를 조회합니다.

                    mapProvider:
                    - NAVER
                    - KAKAO
                    - APPLE
                    """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    schema = Schema(implementation = MapProviderResponse::class),
                    examples = [ExampleObject(
                        name = "success",
                        value = """
                                                    {
                                                      "mapProvider": "KAKAO",
                                                      "updatedAt": "2025-08-10T14:32:00"
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "회원 없음",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "memberNotFound",
                        summary = "NOT_FOUND_MEMBER",
                        value = """
                                                    {
                                                      "errorCode": "NOT_FOUND_MEMBER",
                                                      "message": "회원을 찾을 수 없습니다. MemberId : 42"
                                                    }"""
                    )]
                )]
            ),
        ]
    )
    @GetMapping("/map-provider")
    fun getMapProvider(@RequestAttribute("memberId") memberId: Long): MapProviderResponse

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
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = OnboardingRequest::class),
                examples = [ExampleObject(
                    name = "onboardingRequest",
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
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "온보딩 성공",
                content = [Content(
                    schema = Schema(implementation = OnboardingResponse::class),
                    examples = [ExampleObject(
                        name = "success",
                        value = """
                                                    {
                                                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "updatedAt": "2025-05-10T12:34:56"
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "검증 오류 / 지원하지 않는 값",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
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
                        ExampleObject(
                            name = "unsupportedMapProvider",
                            summary = "UNSUPPORTED_MAP_PROVIDER",
                            value = """
                                                            {
                                                              "errorCode": "UNSUPPORTED_MAP_PROVIDER",
                                                              "message": "지원하지 않는 지도 제공자입니다. MapProvider : ABC"
                                                            }"""
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "질문/답변/회원 없음",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "questionNotFound",
                        summary = "NOT_FOUND_QUESTION",
                        value = """
                                                    {
                                                      "errorCode": "NOT_FOUND_QUESTION",
                                                      "message": "질문을 찾을 수 없습니다. QuestionId : 99"
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 온보딩 완료",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "alreadyOnboarded",
                        summary = "ALREADY_ONBOARDED_MEMBER",
                        value = """
                                                    {
                                                      "errorCode": "ALREADY_ONBOARDED_MEMBER",
                                                      "message": "이미 온보딩을 완료한 회원입니다. MemberId : 42"
                                                    }"""
                    )]
                )]
            ),
        ]
    )
    @PostMapping("/onboarding")
    fun onboarding(
        @RequestAttribute("memberId") memberId: Long,
        mobileType: String,
        @RequestBody request: OnboardingRequest,
    ): OnboardingResponse

    /*──────────────────────────────────────────────────────
     * MapProvider 변경
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "지도 공급자(MapProvider) 변경",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = UpdateMapProviderRequest::class),
                examples = [ExampleObject(value = "{ \"mapProvider\": \"KAKAO\" }")]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "변경 성공",
                content = [Content(
                    schema = Schema(implementation = MapProviderResponse::class),
                    examples = [ExampleObject(
                        name = "success",
                        value = """
                                                    {
                                                      "mapProvider": "KAKAO",
                                                      "updatedAt": "2025-05-11T09:00:00"
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "UNSUPPORTED_MAP_PROVIDER",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        value = """
                                                    {
                                                      "errorCode": "UNSUPPORTED_MAP_PROVIDER",
                                                      "message": "지원하지 않는 지도 제공자입니다. MapProvider : ABC"
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "NOT_FOUND_MEMBER",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        value = """
                                                    {
                                                      "errorCode": "NOT_FOUND_MEMBER",
                                                      "message": "회원을 찾을 수 없습니다. MemberId : 42"
                                                    }"""
                    )]
                )]
            ),
        ]
    )
    @PatchMapping("/map-provider")
    fun updateMapProvider(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: UpdateMapProviderRequest,
    ): MapProviderResponse

    /*──────────────────────────────────────────────────────
     * HOME 주소 수정
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "HOME 주소 수정",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = UpdateHomeAddressRequest::class),
                examples = [ExampleObject(
                    value = """
                                            {
                                              "roadAddress": "서울특별시 강남구 테헤란로 456",
                                              "longitude": 127.0301,
                                              "latitude": 37.4982
                                            }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(
                    schema = Schema(implementation = UpdateHomeAddressResponse::class),
                    examples = [ExampleObject(
                        name = "success",
                        value = """
                                                    {
                                                      "roadAddress": "서울특별시 강남구 테헤란로 456",
                                                      "longitude": 127.0301,
                                                      "latitude": 37.4982
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "NOT_FOUND_HOME_ADDRESS | NOT_FOUND_MEMBER",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        value = """
                                                    {
                                                      "errorCode": "NOT_FOUND_HOME_ADDRESS",
                                                      "message": "회원이 저장한 주소를 찾을 수 없습니다. MemberId : 42"
                                                    }"""
                    )]
                )]
            ),
        ]
    )
    @PatchMapping("/home-address")
    fun updateHomeAddress(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: UpdateHomeAddressRequest,
    ): UpdateHomeAddressResponse

    /*──────────────────────────────────────────────────────
     * 준비 시간 조회
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "회원 준비 시간 조회",
        description = """
                    로그인한 회원의 현재 준비 시간(분 단위)을 조회합니다.
                    준비 시간은 1분에서 240분(4시간) 사이의 값입니다.
                    """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    schema = Schema(implementation = PreparationTimeResponse::class),
                    examples = [ExampleObject(
                        name = "success",
                        value = """
                                                    {
                                                      "preparationTime": 30,
                                                      "updatedAt": "2025-08-30T14:32:00"
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "회원 없음",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "memberNotFound",
                        summary = "NOT_FOUND_MEMBER",
                        value = """
                                                    {
                                                      "errorCode": "NOT_FOUND_MEMBER",
                                                      "message": "회원을 찾을 수 없습니다. MemberId : 42"
                                                    }"""
                    )]
                )]
            ),
        ]
    )
    @GetMapping("/preparation-time")
    fun getPreparationTime(@RequestAttribute("memberId") memberId: Long): PreparationTimeResponse

    /*──────────────────────────────────────────────────────
     * 준비 시간 수정
     *──────────────────────────────────────────────────────*/
    @Operation(
        summary = "회원 준비 시간 수정",
        description = """
                    로그인한 회원의 준비 시간을 변경합니다.
                    준비 시간은 1분에서 240분(4시간) 사이의 값이어야 합니다.
                    """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                schema = Schema(implementation = UpdatePreparationTimeRequest::class),
                examples = [ExampleObject(value = "{ \"preparationTime\": 45 }")]
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(
                    schema = Schema(implementation = PreparationTimeResponse::class),
                    examples = [ExampleObject(
                        name = "success",
                        value = """
                                                    {
                                                      "preparationTime": 45,
                                                      "updatedAt": "2025-08-30T15:20:00"
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "FIELD_ERROR",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        value = """
                                                    {
                                                      "errorCode": "FIELD_ERROR",
                                                      "message": "입력이 잘못되었습니다.",
                                                      "fieldErrors": [
                                                        { "field": "preparationTime", "rejectedValue": 0, "reason": "준비 시간은 최소 1분이어야 합니다." }
                                                      ]
                                                    }"""
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "NOT_FOUND_MEMBER",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        value = """
                                                    {
                                                      "errorCode": "NOT_FOUND_MEMBER",
                                                      "message": "회원을 찾을 수 없습니다. MemberId : 42"
                                                    }"""
                    )]
                )]
            ),
        ]
    )
    @PatchMapping("/preparation-time")
    fun updatePreparationTime(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: UpdatePreparationTimeRequest,
    ): PreparationTimeResponse
}
