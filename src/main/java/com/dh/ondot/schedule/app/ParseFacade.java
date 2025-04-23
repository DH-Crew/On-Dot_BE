package com.dh.ondot.schedule.app;

import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.schedule.api.response.ScheduleParsedResponse;
import com.dh.ondot.schedule.domain.service.AiUsageService;
import com.dh.ondot.schedule.infra.OpenAiPromptApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParseFacade {
    private final MemberService memberService;
    private final AiUsageService aiUsageService;
    private final OpenAiPromptApi openAiPromptApi;

    @Transactional
    public ScheduleParsedResponse parse(Long memberId, String sentence) {
        memberService.findExistingMember(memberId);
        aiUsageService.increaseUsage(memberId);
        return openAiPromptApi.parseNaturalLanguage(sentence);
    }
}
