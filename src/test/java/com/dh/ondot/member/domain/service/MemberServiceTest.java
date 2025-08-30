package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.core.exception.NotFoundMemberException;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 ID로 회원을 조회한다")
    void getMemberIfExists_ValidId_ReturnsMember() {
        // given
        Long memberId = 1L;
        Member member = Member.registerWithOauth("test@example.com", OauthProvider.KAKAO, "kakao123");
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        Member result = memberService.getMemberIfExists(memberId);

        // then
        assertThat(result).isEqualTo(member);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외가 발생한다")
    void getMemberIfExists_InvalidId_ThrowsException() {
        // given
        Long memberId = 999L;
        
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMemberIfExists(memberId))
                .isInstanceOf(NotFoundMemberException.class);
        
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("회원의 준비 시간을 업데이트한다")
    void updatePreparationTime_ValidInput_UpdatesSuccessfully() {
        // given
        Long memberId = 1L;
        Integer newPreparationTime = 45;
        Member member = Member.registerWithOauth("test@example.com", OauthProvider.KAKAO, "kakao123");
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        Member result = memberService.updatePreparationTime(memberId, newPreparationTime);

        // then
        assertThat(result.getPreparationTime()).isEqualTo(newPreparationTime);
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 준비 시간 업데이트 시 예외가 발생한다")
    void updatePreparationTime_InvalidMemberId_ThrowsException() {
        // given
        Long memberId = 999L;
        Integer newPreparationTime = 45;
        
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.updatePreparationTime(memberId, newPreparationTime))
                .isInstanceOf(NotFoundMemberException.class);
        
        verify(memberRepository).findById(memberId);
    }
}
