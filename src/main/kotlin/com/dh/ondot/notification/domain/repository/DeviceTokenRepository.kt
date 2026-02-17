package com.dh.ondot.notification.domain.repository

import com.dh.ondot.notification.domain.DeviceToken
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceTokenRepository : JpaRepository<DeviceToken, Long> {
    fun findByFcmToken(fcmToken: String): DeviceToken?
    fun findAllByMemberId(memberId: Long): List<DeviceToken>
    fun findAllByMemberIdIn(memberIds: List<Long>): List<DeviceToken>
    fun deleteByFcmToken(fcmToken: String)
    fun deleteAllByMemberId(memberId: Long)
}
