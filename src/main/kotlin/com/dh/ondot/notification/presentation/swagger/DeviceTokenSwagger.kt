package com.dh.ondot.notification.presentation.swagger

import com.dh.ondot.core.ErrorResponse
import com.dh.ondot.notification.presentation.request.DeleteDeviceTokenRequest
import com.dh.ondot.notification.presentation.request.RegisterDeviceTokenRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(
    name = "Device Token API",
    description = """
                <b>AccessToken (Authorization: Bearer JWT)</b>은 필수입니다.<br><br>
                FCM 디바이스 토큰 등록/삭제 API.<br>
                앱 실행 시 토큰 등록, 로그아웃 시 토큰 삭제를 수행합니다.
                """
)
@RequestMapping("/device-tokens")
interface DeviceTokenSwagger {

    @Operation(
        summary = "FCM 디바이스 토큰 등록",
        description = "앱 실행 시 FCM 토큰을 서버에 등록합니다. 이미 등록된 토큰이면 소유자 정보를 업데이트합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = RegisterDeviceTokenRequest::class),
                examples = [ExampleObject(
                    value = """
                    {
                      "fcmToken": "example-fcm-device-token",
                      "deviceType": "iOS"
                    }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "등록 완료"),
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
    @PostMapping
    fun registerToken(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: RegisterDeviceTokenRequest,
    )

    @Operation(
        summary = "FCM 디바이스 토큰 삭제",
        description = "로그아웃 시 FCM 토큰을 서버에서 삭제합니다. 본인 소유의 토큰만 삭제 가능합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = DeleteDeviceTokenRequest::class),
                examples = [ExampleObject(
                    value = """{ "fcmToken": "example-fcm-device-token" }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(responseCode = "204", description = "삭제 완료"),
        ]
    )
    @DeleteMapping
    fun deleteToken(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: DeleteDeviceTokenRequest,
    )
}
