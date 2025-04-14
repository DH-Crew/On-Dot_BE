package com.dh.ondot.member.api;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.api.request.UpdateMapProviderRequest;
import com.dh.ondot.member.api.response.HomeAddressResponse;
import com.dh.ondot.member.api.response.OnboardingResponse;
import com.dh.ondot.member.api.response.UpdateMapProviderResponse;
import com.dh.ondot.member.app.MemberFacade;
import com.dh.ondot.member.domain.Address;
import com.dh.ondot.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberFacade memberFacade;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/home-address")
    public HomeAddressResponse getHomeAddress(
            @RequestAttribute("memberId") Long memberId
    ) {
        Address address = memberFacade.getHomeAddress(memberId);
        return HomeAddressResponse.from(address);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/onboarding")
    public OnboardingResponse onboarding(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody OnboardingRequest request
    ) {
        Member member = memberFacade.onboarding(memberId, request);

        return OnboardingResponse.from(member);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/map-provider")
    public UpdateMapProviderResponse updateMapProvider(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody UpdateMapProviderRequest request
    ) {
        Member member = memberFacade.updateMapProvider(memberId, request.mapProvider());

        return UpdateMapProviderResponse.from(member);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping()
    public void deleteMember(
            @RequestAttribute("memberId") Long memberId
    ) {
        memberFacade.deleteMember(memberId);
    }
}
