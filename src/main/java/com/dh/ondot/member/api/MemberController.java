package com.dh.ondot.member.api;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.api.request.UpdateHomeAddressRequest;
import com.dh.ondot.member.api.request.UpdateMapProviderRequest;
import com.dh.ondot.member.api.request.UpdatePreparationTimeRequest;
import com.dh.ondot.member.api.request.WithdrawalRequest;
import com.dh.ondot.member.api.response.HomeAddressResponse;
import com.dh.ondot.member.api.response.OnboardingResponse;
import com.dh.ondot.member.api.response.PreparationTimeResponse;
import com.dh.ondot.member.api.response.UpdateHomeAddressResponse;
import com.dh.ondot.member.api.response.MapProviderResponse;
import com.dh.ondot.member.api.swagger.MemberSwagger;
import com.dh.ondot.member.application.MemberFacade;
import com.dh.ondot.member.domain.Address;
import com.dh.ondot.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController implements MemberSwagger {
    private final MemberFacade memberFacade;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteMember(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody WithdrawalRequest request
    ) {
        memberFacade.deleteMember(memberId, request.withdrawalReasonId(), request.customReason());
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/onboarding")
    public OnboardingResponse onboarding(
            @RequestAttribute("memberId") Long memberId,
            @RequestHeader(value = "X-Mobile-Type", required = false) String mobileType,
            @Valid @RequestBody OnboardingRequest request
    ) {
        return memberFacade.onboarding(memberId, mobileType, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/home-address")
    public HomeAddressResponse getHomeAddress(
            @RequestAttribute("memberId") Long memberId
    ) {
        Address address = memberFacade.getHomeAddress(memberId);
        return HomeAddressResponse.from(address);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/map-provider")
    public MapProviderResponse getMapProvider(
            @RequestAttribute("memberId") Long memberId
    ) {
        Member member = memberFacade.getMember(memberId);
        return MapProviderResponse.from(member);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/map-provider")
    public MapProviderResponse updateMapProvider(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody UpdateMapProviderRequest request
    ) {
        Member member = memberFacade.updateMapProvider(memberId, request.mapProvider());

        return MapProviderResponse.from(member);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/home-address")
    public UpdateHomeAddressResponse updateHomeAddress(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody UpdateHomeAddressRequest request
    ) {
        Address address = memberFacade.updateHomeAddress(
                memberId,
                request.roadAddress(),
                request.longitude(),
                request.latitude()
        );

        return UpdateHomeAddressResponse.from(address);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/preparation-time")
    public PreparationTimeResponse getPreparationTime(
            @RequestAttribute("memberId") Long memberId
    ) {
        Member member = memberFacade.getMember(memberId);
        return PreparationTimeResponse.from(member);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/preparation-time")
    public PreparationTimeResponse updatePreparationTime(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody UpdatePreparationTimeRequest request
    ) {
        Member member = memberFacade.updatePreparationTime(memberId, request.preparationTime());
        return PreparationTimeResponse.from(member);
    }
}
