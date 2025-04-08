package com.dh.ondot.member.api;

import com.dh.ondot.member.app.MemberFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberFacade memberFacade;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping()
    public void deleteMember(
            @RequestAttribute("memberId") Long memberId
    ) {
        memberFacade.deleteMember(memberId);
    }
}
