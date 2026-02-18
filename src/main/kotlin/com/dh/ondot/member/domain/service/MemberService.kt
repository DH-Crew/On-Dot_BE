package com.dh.ondot.member.domain.service

import com.dh.ondot.member.application.command.OnboardingCommand
import com.dh.ondot.member.core.exception.AlreadyOnboardedException
import com.dh.ondot.member.core.exception.NotFoundMemberException
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.dto.UserInfo
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.domain.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun getMemberIfExists(memberId: Long): Member =
        memberRepository.findById(memberId)
            .orElseThrow { NotFoundMemberException(memberId) }

    fun getAndValidateAlreadyOnboarded(memberId: Long): Member {
        val member = getMemberIfExists(memberId)
        if (!member.isNewMember()) {
            throw AlreadyOnboardedException(member.id)
        }
        return member
    }

    fun getTotalMemberCount(): Long = memberRepository.count()

    @Transactional
    fun findOrRegisterOauthMember(userInfo: UserInfo, oauthProvider: OauthProvider): Member =
        memberRepository.findByOauthInfo(userInfo.oauthProviderId, oauthProvider)
            ?: registerMemberWithOauth(userInfo, oauthProvider)

    private fun registerMemberWithOauth(userInfo: UserInfo, oauthProvider: OauthProvider): Member {
        val newMember = Member.registerWithOauth(
            userInfo.email.orEmpty(),
            oauthProvider,
            userInfo.oauthProviderId,
        )
        return memberRepository.save(newMember)
    }

    @Transactional
    fun updatePreparationTime(memberId: Long, preparationTime: Int): Member {
        val member = getMemberIfExists(memberId)
        member.updatePreparationTime(preparationTime)
        return member
    }

    @Transactional
    fun updateOnboardingInfo(member: Member, command: OnboardingCommand): Member {
        member.updateOnboarding(
            command.preparationTime, command.alarmMode,
            command.isSnoozeEnabled, command.snoozeInterval, command.snoozeCount,
            command.soundCategory, command.ringTone, command.volume,
        )
        return member
    }

    @Transactional
    fun deleteMember(memberId: Long) {
        memberRepository.deleteById(memberId)
    }

    @Transactional
    fun updateDailyReminderEnabled(memberId: Long, enabled: Boolean): Member {
        val member = getMemberIfExists(memberId)
        member.updateDailyReminderEnabled(enabled)
        return member
    }

    fun findAllDailyReminderEnabledMembers(): List<Member> =
        memberRepository.findAllByDailyReminderEnabledTrue()
}
