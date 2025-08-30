package com.dh.ondot.member.domain.repository;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.enums.OauthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m WHERE m.oauthInfo.oauthProviderId = :providerId AND m.oauthInfo.oauthProvider = :provider")
    Optional<Member> findByOauthInfo(@Param("providerId") String providerId, @Param("provider") OauthProvider provider);
}
