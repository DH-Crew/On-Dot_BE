package com.dh.ondot.member.application

import com.dh.ondot.member.api.request.OnboardingRequest
import com.dh.ondot.member.api.response.OnboardingResponse
import com.dh.ondot.member.application.command.CreateAddressCommand
import com.dh.ondot.member.application.command.CreateChoicesCommand
import com.dh.ondot.member.application.command.OnboardingCommand
import com.dh.ondot.member.application.dto.Token
import com.dh.ondot.member.domain.Address
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.domain.event.UserRegistrationEvent
import com.dh.ondot.member.domain.service.AddressService
import com.dh.ondot.member.domain.service.ChoiceService
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.member.domain.service.WithdrawalService
import com.dh.ondot.schedule.domain.service.ScheduleService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberFacade(
    private val tokenFacade: TokenFacade,
    private val memberService: MemberService,
    private val addressService: AddressService,
    private val choiceService: ChoiceService,
    private val scheduleService: ScheduleService,
    private val withdrawalService: WithdrawalService,
    private val eventPublisher: ApplicationEventPublisher,
) {

    fun getMember(memberId: Long): Member =
        memberService.getMemberIfExists(memberId)

    @Transactional(readOnly = true)
    fun getHomeAddress(memberId: Long): Address {
        memberService.getMemberIfExists(memberId)
        return addressService.getHomeAddress(memberId)
    }

    @Transactional
    fun onboarding(memberId: Long, mobileType: String, request: OnboardingRequest): OnboardingResponse {
        val member = memberService.getAndValidateAlreadyOnboarded(memberId)

        val onboardingCommand = OnboardingCommand.from(request)
        val addressCommand = CreateAddressCommand.from(request)
        val choicesCommand = CreateChoicesCommand.from(request)

        memberService.updateOnboardingInfo(member, onboardingCommand)
        addressService.createHomeAddress(member, addressCommand)
        choiceService.createChoices(member, choicesCommand)

        val token: Token = tokenFacade.issue(member.id)

        publishUserRegistrationEvent(member, member.oauthInfo.oauthProvider, mobileType)

        return OnboardingResponse.from(token.accessToken, token.refreshToken, member)
    }

    private fun publishUserRegistrationEvent(member: Member, oauthProvider: OauthProvider, mobileType: String) {
        val totalMemberCount = memberService.getTotalMemberCount()

        val event = UserRegistrationEvent(
            member.id,
            member.email,
            oauthProvider,
            totalMemberCount,
            mobileType,
        )

        eventPublisher.publishEvent(event)
    }

    @Transactional
    fun updateMapProvider(memberId: Long, mapProvider: String): Member {
        val member = memberService.getMemberIfExists(memberId)
        member.updateMapProvider(mapProvider)

        return member
    }

    @Transactional
    fun updateHomeAddress(
        memberId: Long,
        roadAddress: String,
        longitude: Double,
        latitude: Double,
    ): Address {
        memberService.getMemberIfExists(memberId)
        val command = CreateAddressCommand(roadAddress, longitude, latitude)
        return addressService.updateHomeAddress(memberId, command)
    }

    fun updatePreparationTime(memberId: Long, preparationTime: Int): Member =
        memberService.updatePreparationTime(memberId, preparationTime)

    @Transactional
    fun deleteMember(memberId: Long, withdrawalReasonId: Long, customReason: String) {
        memberService.getMemberIfExists(memberId)
        withdrawalService.saveWithdrawalReason(memberId, withdrawalReasonId, customReason)

        scheduleService.deleteAllByMemberId(memberId)
        addressService.deleteAllByMemberId(memberId)
        choiceService.deleteAllByMemberId(memberId)
        memberService.deleteMember(memberId)
    }
}
