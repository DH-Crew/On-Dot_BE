package com.dh.ondot.notification.application

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.notification.domain.DeviceToken
import com.dh.ondot.notification.domain.service.DeviceTokenService
import com.dh.ondot.notification.infra.fcm.FcmClient
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
@DisplayName("DailyReminderScheduler 테스트")
class DailyReminderSchedulerTest {

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var scheduleRepository: ScheduleRepository

    @Mock
    private lateinit var deviceTokenService: DeviceTokenService

    @Mock
    private lateinit var fcmClient: FcmClient

    @InjectMocks
    private lateinit var scheduler: DailyReminderScheduler

    @Test
    @DisplayName("내일 일정이 있는 회원에게 리마인더를 전송한다")
    fun sendDailyReminder_MembersWithSchedules_SendsPush() {
        // given
        val member = createMember(1L)
        given(memberService.findAllDailyReminderEnabledMembers()).willReturn(listOf(member))

        val tomorrow = TimeUtils.nowSeoulDate().plusDays(1)
        val appointmentAt = TimeUtils.toInstant(tomorrow.atTime(10, 0))
        val schedule = Schedule(memberId = 1L, appointmentAt = appointmentAt)

        given(scheduleRepository.findAllByMemberIdInAndAppointmentAtRange(
            eq(listOf(1L)), any(), any()
        )).willReturn(listOf(schedule))

        given(scheduleRepository.findAllByMemberIdInAndIsRepeatTrue(
            eq(listOf(1L))
        )).willReturn(emptyList())

        val token = DeviceToken.create(1L, "token-abc", "iOS")
        given(deviceTokenService.findAllByMemberIds(listOf(1L))).willReturn(listOf(token))
        given(fcmClient.sendToTokens(any(), any(), any())).willReturn(emptyList())

        // when
        scheduler.sendDailyReminder()

        // then
        verify(fcmClient).sendToTokens(
            eq(listOf("token-abc")),
            any(),
            eq("내일 1개의 일정이 예정되어 있어요")
        )
    }

    @Test
    @DisplayName("내일 일정이 없으면 푸시를 보내지 않는다")
    fun sendDailyReminder_NoSchedules_SkipsPush() {
        // given
        val member = createMember(1L)
        given(memberService.findAllDailyReminderEnabledMembers()).willReturn(listOf(member))
        given(scheduleRepository.findAllByMemberIdInAndAppointmentAtRange(
            eq(listOf(1L)), any(), any()
        )).willReturn(emptyList())
        given(scheduleRepository.findAllByMemberIdInAndIsRepeatTrue(
            eq(listOf(1L))
        )).willReturn(emptyList())

        // when
        scheduler.sendDailyReminder()

        // then
        verify(fcmClient, never()).sendToTokens(any(), any(), any())
    }

    @Test
    @DisplayName("리마인더 활성 회원이 없으면 아무 작업도 하지 않는다")
    fun sendDailyReminder_NoEnabledMembers_DoesNothing() {
        // given
        given(memberService.findAllDailyReminderEnabledMembers()).willReturn(emptyList())

        // when
        scheduler.sendDailyReminder()

        // then
        verify(scheduleRepository, never()).findAllByMemberIdInAndAppointmentAtRange(any(), any(), any())
        verify(fcmClient, never()).sendToTokens(any(), any(), any())
    }

    private fun createMember(id: Long): Member {
        val member = Member.registerWithOauth("test$id@example.com", OauthProvider.KAKAO, "kakao$id")
        val idField = Member::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(member, id)
        return member
    }
}
