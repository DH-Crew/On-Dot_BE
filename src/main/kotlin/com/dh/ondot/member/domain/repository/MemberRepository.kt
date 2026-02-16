package com.dh.ondot.member.domain.repository

import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.OauthProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MemberRepository : JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m WHERE m.oauthInfo.oauthProviderId = :providerId AND m.oauthInfo.oauthProvider = :provider")
    fun findByOauthInfo(
        @Param("providerId") providerId: String,
        @Param("provider") provider: OauthProvider,
    ): Member?
}
