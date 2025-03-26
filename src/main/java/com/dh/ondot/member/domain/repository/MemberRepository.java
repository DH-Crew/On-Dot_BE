package com.dh.ondot.member.domain.repository;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.OauthInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);
}