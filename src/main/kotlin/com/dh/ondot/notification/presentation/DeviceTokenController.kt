package com.dh.ondot.notification.presentation

import com.dh.ondot.notification.application.DeviceTokenFacade
import com.dh.ondot.notification.presentation.request.DeleteDeviceTokenRequest
import com.dh.ondot.notification.presentation.request.RegisterDeviceTokenRequest
import com.dh.ondot.notification.presentation.swagger.DeviceTokenSwagger
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/device-tokens")
class DeviceTokenController(
    private val deviceTokenFacade: DeviceTokenFacade,
) : DeviceTokenSwagger {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    override fun registerToken(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: RegisterDeviceTokenRequest,
    ) {
        deviceTokenFacade.registerToken(request.toCommand(memberId))
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    override fun deleteToken(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: DeleteDeviceTokenRequest,
    ) {
        deviceTokenFacade.deleteToken(request.fcmToken)
    }
}
