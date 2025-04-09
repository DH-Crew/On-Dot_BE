package com.dh.ondot.member.api;

import com.dh.ondot.member.api.request.OnboardingRequest;
import com.dh.ondot.member.api.response.OnboardingResponse;
import com.dh.ondot.member.app.MemberFacade;
import com.dh.ondot.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberFacade memberFacade;

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/onboarding")
    public OnboardingResponse onboarding(
            @RequestAttribute("memberId") Long memberId,
            @RequestBody OnboardingRequest request
    ) {
        Member member = memberFacade.onboarding(memberId, request);

        return OnboardingResponse.from(member);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping()
    public void deleteMember(
            @RequestAttribute("memberId") Long memberId
    ) {
        memberFacade.deleteMember(memberId);
    }
}
