package com.dh.ondot.notification.domain.service

import com.dh.ondot.notification.domain.DeviceToken
import com.dh.ondot.notification.domain.repository.DeviceTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DeviceTokenService(
    private val deviceTokenRepository: DeviceTokenRepository,
) {

    @Transactional
    fun registerOrUpdate(memberId: Long, fcmToken: String, deviceType: String) {
        val existing = deviceTokenRepository.findByFcmToken(fcmToken)
        if (existing == null) {
            deviceTokenRepository.save(DeviceToken.create(memberId, fcmToken, deviceType))
        } else if (existing.memberId != memberId || existing.deviceType != deviceType) {
            existing.updateOwner(memberId, deviceType)
        }
    }

    @Transactional
    fun deleteByMemberAndFcmToken(memberId: Long, fcmToken: String) {
        val token = deviceTokenRepository.findByFcmToken(fcmToken) ?: return
        if (token.memberId == memberId) {
            deviceTokenRepository.deleteByFcmToken(fcmToken)
        }
    }

    @Transactional
    fun deleteByFcmToken(fcmToken: String) {
        deviceTokenRepository.deleteByFcmToken(fcmToken)
    }

    @Transactional
    fun deleteAllByMemberId(memberId: Long) {
        deviceTokenRepository.deleteAllByMemberId(memberId)
    }

    fun findAllByMemberIds(memberIds: List<Long>): List<DeviceToken> =
        deviceTokenRepository.findAllByMemberIdIn(memberIds)

    @Transactional
    fun deleteByFcmTokens(fcmTokens: List<String>) {
        fcmTokens.forEach { deviceTokenRepository.deleteByFcmToken(it) }
    }
}
