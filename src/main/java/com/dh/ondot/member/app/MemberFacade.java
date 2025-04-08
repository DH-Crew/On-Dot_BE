package com.dh.ondot.member.app;

import com.dh.ondot.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.dh.ondot.member.app.MemberServiceHelper.findExistingMember;

@Service
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberRepository memberRepository;

    public void deleteMember(Long memberId) {
        findExistingMember(memberRepository, memberId);
        memberRepository.deleteById(memberId);
    }
}
