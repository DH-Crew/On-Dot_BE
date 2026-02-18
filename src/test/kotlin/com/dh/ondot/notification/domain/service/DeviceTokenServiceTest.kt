package com.dh.ondot.notification.domain.service

import com.dh.ondot.notification.domain.DeviceToken
import com.dh.ondot.notification.domain.repository.DeviceTokenRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
@DisplayName("DeviceTokenService 테스트")
class DeviceTokenServiceTest {

    @Mock
    private lateinit var deviceTokenRepository: DeviceTokenRepository

    @InjectMocks
    private lateinit var deviceTokenService: DeviceTokenService

    @Test
    @DisplayName("신규 FCM 토큰을 등록한다")
    fun registerToken_NewToken_SavesNewToken() {
        // given
        val memberId = 1L
        val fcmToken = "new-token-123"
        val deviceType = "iOS"
        given(deviceTokenRepository.findByFcmToken(fcmToken)).willReturn(null)
        given(deviceTokenRepository.save(any<DeviceToken>())).willAnswer { it.arguments[0] }

        // when
        deviceTokenService.registerOrUpdate(memberId, fcmToken, deviceType)

        // then
        verify(deviceTokenRepository).findByFcmToken(fcmToken)
        verify(deviceTokenRepository).save(any<DeviceToken>())
    }

    @Test
    @DisplayName("같은 회원의 기존 FCM 토큰이 존재하면 새로 저장하지 않는다")
    fun registerToken_ExistingTokenSameMember_SkipsSave() {
        // given
        val memberId = 1L
        val fcmToken = "existing-token-123"
        val deviceType = "iOS"
        val existing = DeviceToken.create(memberId, fcmToken, deviceType)
        given(deviceTokenRepository.findByFcmToken(fcmToken)).willReturn(existing)

        // when
        deviceTokenService.registerOrUpdate(memberId, fcmToken, deviceType)

        // then
        verify(deviceTokenRepository).findByFcmToken(fcmToken)
        verify(deviceTokenRepository, never()).save(any<DeviceToken>())
    }

    @Test
    @DisplayName("다른 회원의 FCM 토큰이 존재하면 소유자를 업데이트한다")
    fun registerToken_ExistingTokenDifferentMember_UpdatesOwner() {
        // given
        val originalMemberId = 1L
        val newMemberId = 2L
        val fcmToken = "existing-token-123"
        val existing = DeviceToken.create(originalMemberId, fcmToken, "iOS")
        given(deviceTokenRepository.findByFcmToken(fcmToken)).willReturn(existing)

        // when
        deviceTokenService.registerOrUpdate(newMemberId, fcmToken, "Android")

        // then
        verify(deviceTokenRepository).findByFcmToken(fcmToken)
        verify(deviceTokenRepository, never()).save(any<DeviceToken>())
        assertThat(existing.memberId).isEqualTo(newMemberId)
        assertThat(existing.deviceType).isEqualTo("Android")
    }

    @Test
    @DisplayName("본인 소유의 토큰만 삭제한다")
    fun deleteByMemberAndFcmToken_OwnToken_Deletes() {
        // given
        val memberId = 1L
        val fcmToken = "token-to-delete"
        val token = DeviceToken.create(memberId, fcmToken, "iOS")
        given(deviceTokenRepository.findByFcmToken(fcmToken)).willReturn(token)

        // when
        deviceTokenService.deleteByMemberAndFcmToken(memberId, fcmToken)

        // then
        verify(deviceTokenRepository).deleteByFcmToken(fcmToken)
    }

    @Test
    @DisplayName("다른 회원의 토큰은 삭제하지 않는다")
    fun deleteByMemberAndFcmToken_OtherMemberToken_SkipsDelete() {
        // given
        val ownerId = 1L
        val requesterId = 2L
        val fcmToken = "other-member-token"
        val token = DeviceToken.create(ownerId, fcmToken, "iOS")
        given(deviceTokenRepository.findByFcmToken(fcmToken)).willReturn(token)

        // when
        deviceTokenService.deleteByMemberAndFcmToken(requesterId, fcmToken)

        // then
        verify(deviceTokenRepository, never()).deleteByFcmToken(fcmToken)
    }

    @Test
    @DisplayName("회원의 모든 디바이스 토큰을 조회한다")
    fun findAllByMemberIds_ReturnsTokens() {
        // given
        val memberIds = listOf(1L, 2L)
        val tokens = listOf(
            DeviceToken.create(1L, "token-1", "iOS"),
            DeviceToken.create(2L, "token-2", "Android"),
        )
        given(deviceTokenRepository.findAllByMemberIdIn(memberIds)).willReturn(tokens)

        // when
        val result = deviceTokenService.findAllByMemberIds(memberIds)

        // then
        assertThat(result).hasSize(2)
        verify(deviceTokenRepository).findAllByMemberIdIn(memberIds)
    }
}
