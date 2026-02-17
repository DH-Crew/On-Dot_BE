package com.dh.ondot.notification.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.notification.application.dto.RegisterDeviceTokenCommand
import com.dh.ondot.notification.domain.service.DeviceTokenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceTokenFacade(
    private val memberService: MemberService,
    private val deviceTokenService: DeviceTokenService,
) {

    @Transactional
    fun registerToken(command: RegisterDeviceTokenCommand) {
        memberService.getMemberIfExists(command.memberId)
        deviceTokenService.registerOrUpdate(command.memberId, command.fcmToken, command.deviceType)
    }

    @Transactional
    fun deleteToken(fcmToken: String) {
        deviceTokenService.deleteByFcmToken(fcmToken)
    }
}
