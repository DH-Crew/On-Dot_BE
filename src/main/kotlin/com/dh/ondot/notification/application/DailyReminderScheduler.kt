package com.dh.ondot.notification.application

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.notification.domain.service.DeviceTokenService
import com.dh.ondot.notification.infra.fcm.FcmClient
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DailyReminderScheduler(
    private val memberService: MemberService,
    private val scheduleRepository: ScheduleRepository,
    private val deviceTokenService: DeviceTokenService,
    private val fcmClient: FcmClient,
) {
    companion object {
        private const val EVERY_DAY_10PM_KST = "0 0 22 * * *"
        private const val PUSH_TITLE = "온닷"
    }

    @Scheduled(cron = EVERY_DAY_10PM_KST, zone = "Asia/Seoul")
    fun sendDailyReminder() {
        val enabledMembers = memberService.findAllDailyReminderEnabledMembers()
        if (enabledMembers.isEmpty()) return

        val memberIds = enabledMembers.map { it.id }
        val tomorrow = TimeUtils.nowSeoulDate().plusDays(1)

        val scheduleCountByMember = countSchedulesForDate(memberIds, tomorrow)
        if (scheduleCountByMember.isEmpty()) return

        val memberIdsWithSchedules = scheduleCountByMember.keys.toList()
        val tokens = deviceTokenService.findAllByMemberIds(memberIdsWithSchedules)
        val tokensByMember = tokens.groupBy { it.memberId }

        for ((memberId, count) in scheduleCountByMember) {
            val memberTokens = tokensByMember[memberId] ?: continue
            val fcmTokens = memberTokens.map { it.fcmToken }
            val body = "내일 ${count}개의 일정이 예정되어 있어요"

            val invalidTokens = fcmClient.sendToTokens(fcmTokens, PUSH_TITLE, body)
            if (invalidTokens.isNotEmpty()) {
                deviceTokenService.deleteByFcmTokens(invalidTokens)
            }
        }
    }

    private fun countSchedulesForDate(memberIds: List<Long>, date: LocalDate): Map<Long, Int> {
        val startOfDay = TimeUtils.toInstant(date.atStartOfDay())
        val endOfDay = TimeUtils.toInstant(date.plusDays(1).atStartOfDay())

        // 단발성 일정 (반복 일정 제외)
        val singleSchedules = scheduleRepository
            .findAllByMemberIdInAndAppointmentAtRange(memberIds, startOfDay, endOfDay)
            .filter { !it.isRepeat }

        // 반복 일정
        val repeatSchedules = scheduleRepository
            .findAllByMemberIdInAndIsRepeatTrue(memberIds)
            .filter { isScheduledForDate(it, date) }

        val allSchedules = singleSchedules + repeatSchedules
        return allSchedules.groupBy { it.memberId }.mapValues { it.value.size }
    }

    private fun isScheduledForDate(schedule: Schedule, date: LocalDate): Boolean {
        val dayValue = (date.dayOfWeek.value % 7) + 1
        return schedule.repeatDays?.contains(dayValue) == true
    }
}
