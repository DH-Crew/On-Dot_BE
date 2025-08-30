package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.application.command.OnboardingCommand;
import com.dh.ondot.member.core.exception.AlreadyOnboardedException;
import com.dh.ondot.member.core.exception.NotFoundMemberException;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.dto.UserInfo;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.member.domain.repository.MemberRepository;
import com.dh.ondot.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    @DisplayName("신규 회원이 아닌 경우 온보딩 여부 검증 시 예외가 발생한다")
    void getAndValidateAlreadyOnboarded_AlreadyOnboardedMember_ThrowsException() {
        // given
        Long memberId = 1L;
        Member onboardedMember = MemberFixture.onboardedMember();
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(onboardedMember));

        // when & then
        assertThatThrownBy(() -> memberService.getAndValidateAlreadyOnboarded(memberId))
                .isInstanceOf(AlreadyOnboardedException.class);
        
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("신규 회원인 경우 온보딩 여부 검증을 통과한다")
    void getAndValidateAlreadyOnboarded_NewMember_ReturnsMembe() {
        // given
        Long memberId = 1L;
        Member newMember = MemberFixture.newMember();
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(newMember));

        // when
        Member result = memberService.getAndValidateAlreadyOnboarded(memberId);

        // then
        assertThat(result).isEqualTo(newMember);
        assertThat(result.isNewMember()).isTrue();
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("OAuth 회원을 조회하고 존재하지 않으면 새로 등록한다")
    void findOrRegisterOauthMember_NewUser_RegistersAndReturns() {
        // given
        UserInfo userInfo = new UserInfo("kakao789", "new@example.com");
        OauthProvider provider = OauthProvider.KAKAO;
        Member newMember = Member.registerWithOauth("new@example.com", provider, "kakao789");
        
        given(memberRepository.findByOauthInfo(provider, "kakao789")).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willReturn(newMember);

        // when
        Member result = memberService.findOrRegisterOauthMember(userInfo, provider);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(memberRepository).findByOauthInfo(provider, "kakao789");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("기존 OAuth 회원을 조회한다")
    void findOrRegisterOauthMember_ExistingUser_ReturnsExisting() {
        // given
        UserInfo userInfo = new UserInfo("kakao456", "existing@example.com");
        OauthProvider provider = OauthProvider.KAKAO;
        Member existingMember = MemberFixture.onboardedMember();
        
        given(memberRepository.findByOauthInfo(provider, "kakao456")).willReturn(Optional.of(existingMember));

        // when
        Member result = memberService.findOrRegisterOauthMember(userInfo, provider);

        // then
        assertThat(result).isEqualTo(existingMember);
        verify(memberRepository).findByOauthInfo(provider, "kakao456");
    }

    @Test
    @DisplayName("회원의 온보딩 정보를 업데이트한다")
    void updateOnboardingInfo_ValidInput_UpdatesSuccessfully() {
        // given
        Member member = MemberFixture.newMember();
        OnboardingCommand command = new OnboardingCommand(
                30, "SOUND", true, 5, 3, 
                "BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5
        );

        // when
        Member result = memberService.updateOnboardingInfo(member, command);

        // then
        assertThat(result).isEqualTo(member);
        assertThat(result.getPreparationTime()).isEqualTo(30);
        assertThat(result.isNewMember()).isFalse();
    }

    @Test
    @DisplayName("회원을 삭제한다")
    void deleteMember_ValidMemberId_DeletesSuccessfully() {
        // given
        Long memberId = 1L;

        // when
        memberService.deleteMember(memberId);

        // then
        verify(memberRepository).deleteById(memberId);
    }
}
