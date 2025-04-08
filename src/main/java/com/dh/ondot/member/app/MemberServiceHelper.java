package com.dh.ondot.member.app;

import com.dh.ondot.member.core.exception.MemberNotFoundException;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.repository.MemberRepository;

public final class MemberServiceHelper {
    public static Member findExistingMember(MemberRepository memberRepository, Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }
}
