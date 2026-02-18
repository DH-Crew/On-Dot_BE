package com.dh.ondot.member.presentation

import com.dh.ondot.member.application.MemberFacade
import com.dh.ondot.member.application.command.DeleteMemberCommand
import com.dh.ondot.member.application.command.UpdateHomeAddressCommand
import com.dh.ondot.member.presentation.request.OnboardingRequest
import com.dh.ondot.member.presentation.request.UpdateHomeAddressRequest
import com.dh.ondot.member.presentation.request.UpdateMapProviderRequest
import com.dh.ondot.member.presentation.request.UpdatePreparationTimeRequest
import com.dh.ondot.member.presentation.request.WithdrawalRequest
import com.dh.ondot.member.presentation.response.HomeAddressResponse
import com.dh.ondot.member.presentation.response.MapProviderResponse
import com.dh.ondot.member.presentation.response.OnboardingResponse
import com.dh.ondot.member.presentation.response.PreparationTimeResponse
import com.dh.ondot.member.presentation.response.UpdateHomeAddressResponse
import com.dh.ondot.member.presentation.request.UpdateDailyReminderRequest
import com.dh.ondot.member.presentation.response.DailyReminderResponse
import com.dh.ondot.member.presentation.swagger.MemberSwagger
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/members")
class MemberController(
    private val memberFacade: MemberFacade,
) : MemberSwagger {

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    override fun deleteMember(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: WithdrawalRequest,
    ) {
        memberFacade.deleteMember(memberId, DeleteMemberCommand(request.withdrawalReasonId, request.customReason))
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/onboarding")
    override fun onboarding(
        @RequestAttribute("memberId") memberId: Long,
        @RequestHeader(value = "X-Mobile-Type", required = false) mobileType: String,
        @Valid @RequestBody request: OnboardingRequest,
    ): OnboardingResponse {
        val result = memberFacade.onboarding(
            memberId, mobileType,
            request.toOnboardingCommand(),
            request.toAddressCommand(),
            request.toChoicesCommand(),
        )
        return OnboardingResponse.from(result)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/home-address")
    override fun getHomeAddress(
        @RequestAttribute("memberId") memberId: Long,
    ): HomeAddressResponse {
        val address = memberFacade.getHomeAddress(memberId)
        return HomeAddressResponse.from(address)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/map-provider")
    override fun getMapProvider(
        @RequestAttribute("memberId") memberId: Long,
    ): MapProviderResponse {
        val member = memberFacade.getMember(memberId)
        return MapProviderResponse.from(member)
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/map-provider")
    override fun updateMapProvider(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: UpdateMapProviderRequest,
    ): MapProviderResponse {
        val member = memberFacade.updateMapProvider(memberId, request.mapProvider)
        return MapProviderResponse.from(member)
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/home-address")
    override fun updateHomeAddress(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: UpdateHomeAddressRequest,
    ): UpdateHomeAddressResponse {
        val command = UpdateHomeAddressCommand(request.roadAddress, request.longitude, request.latitude)
        val address = memberFacade.updateHomeAddress(memberId, command)
        return UpdateHomeAddressResponse.from(address)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/preparation-time")
    override fun getPreparationTime(
        @RequestAttribute("memberId") memberId: Long,
    ): PreparationTimeResponse {
        val member = memberFacade.getMember(memberId)
        return PreparationTimeResponse.from(member)
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/preparation-time")
    override fun updatePreparationTime(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: UpdatePreparationTimeRequest,
    ): PreparationTimeResponse {
        val member = memberFacade.updatePreparationTime(memberId, request.preparationTime)
        return PreparationTimeResponse.from(member)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/daily-reminder")
    override fun getDailyReminder(@RequestAttribute("memberId") memberId: Long): DailyReminderResponse {
        val member = memberFacade.getMember(memberId)
        return DailyReminderResponse.from(member.dailyReminderEnabled)
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/daily-reminder")
    override fun updateDailyReminder(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: UpdateDailyReminderRequest,
    ): DailyReminderResponse {
        val member = memberFacade.updateDailyReminderEnabled(memberId, request.enabled!!)
        return DailyReminderResponse.from(member.dailyReminderEnabled)
    }
}
