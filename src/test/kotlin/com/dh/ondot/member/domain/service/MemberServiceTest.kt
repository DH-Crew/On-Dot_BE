package com.dh.ondot.member.domain.service

import com.dh.ondot.member.application.command.OnboardingCommand
import com.dh.ondot.member.core.exception.AlreadyOnboardedException
import com.dh.ondot.member.core.exception.NotFoundMemberException
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.dto.UserInfo
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.domain.repository.MemberRepository
import com.dh.ondot.member.fixture.MemberFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @Mock
    private lateinit var memberRepository: MemberRepository

    @InjectMocks
    private lateinit var memberService: MemberService

    @Test
    @DisplayName("회원 ID로 회원을 조회한다")
    fun getMemberIfExists_ValidId_ReturnsMember() {
        // given
        val memberId = 1L
        val member = Member.registerWithOauth("test@example.com", OauthProvider.KAKAO, "kakao123")

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

        // when
        val result = memberService.getMemberIfExists(memberId)

        // then
        assertThat(result).isEqualTo(member)
        assertThat(result.email).isEqualTo("test@example.com")
        verify(memberRepository).findById(memberId)
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외가 발생한다")
    fun getMemberIfExists_InvalidId_ThrowsException() {
        // given
        val memberId = 999L

        given(memberRepository.findById(memberId)).willReturn(Optional.empty())

        // when & then
        assertThatThrownBy { memberService.getMemberIfExists(memberId) }
            .isInstanceOf(NotFoundMemberException::class.java)

        verify(memberRepository).findById(memberId)
    }

    @Test
    @DisplayName("회원의 준비 시간을 업데이트한다")
    fun updatePreparationTime_ValidInput_UpdatesSuccessfully() {
        // given
        val memberId = 1L
        val newPreparationTime = 45
        val member = Member.registerWithOauth("test@example.com", OauthProvider.KAKAO, "kakao123")

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

        // when
        val result = memberService.updatePreparationTime(memberId, newPreparationTime)

        // then
        assertThat(result.preparationTime).isEqualTo(newPreparationTime)
        verify(memberRepository).findById(memberId)
    }

    @Test
    @DisplayName("존재하지 않는 회원의 준비 시간 업데이트 시 예외가 발생한다")
    fun updatePreparationTime_InvalidMemberId_ThrowsException() {
        // given
        val memberId = 999L
        val newPreparationTime = 45

        given(memberRepository.findById(memberId)).willReturn(Optional.empty())

        // when & then
        assertThatThrownBy { memberService.updatePreparationTime(memberId, newPreparationTime) }
            .isInstanceOf(NotFoundMemberException::class.java)

        verify(memberRepository).findById(memberId)
    }

    @Test
    @DisplayName("신규 회원이 아닌 경우 온보딩 여부 검증 시 예외가 발생한다")
    fun getAndValidateAlreadyOnboarded_AlreadyOnboardedMember_ThrowsException() {
        // given
        val memberId = 1L
        val onboardedMember = MemberFixture.onboardedMember()

        given(memberRepository.findById(memberId)).willReturn(Optional.of(onboardedMember))

        // when & then
        assertThatThrownBy { memberService.getAndValidateAlreadyOnboarded(memberId) }
            .isInstanceOf(AlreadyOnboardedException::class.java)

        verify(memberRepository).findById(memberId)
    }

    @Test
    @DisplayName("신규 회원인 경우 온보딩 여부 검증을 통과한다")
    fun getAndValidateAlreadyOnboarded_NewMember_ReturnsMembe() {
        // given
        val memberId = 1L
        val newMember = MemberFixture.newMember()

        given(memberRepository.findById(memberId)).willReturn(Optional.of(newMember))

        // when
        val result = memberService.getAndValidateAlreadyOnboarded(memberId)

        // then
        assertThat(result).isEqualTo(newMember)
        assertThat(result.isNewMember()).isTrue()
        verify(memberRepository).findById(memberId)
    }

    @Test
    @DisplayName("OAuth 회원을 조회하고 존재하지 않으면 새로 등록한다")
    fun findOrRegisterOauthMember_NewUser_RegistersAndReturns() {
        // given
        val userInfo = UserInfo("kakao789", "new@example.com")
        val provider = OauthProvider.KAKAO
        val newMember = Member.registerWithOauth("new@example.com", provider, "kakao789")

        given(memberRepository.findByOauthInfo("kakao789", provider)).willReturn(null)
        given(memberRepository.save(any(Member::class.java))).willReturn(newMember)

        // when
        val result = memberService.findOrRegisterOauthMember(userInfo, provider)

        // then
        assertThat(result).isNotNull()
        assertThat(result.email).isEqualTo("new@example.com")
        verify(memberRepository).findByOauthInfo("kakao789", provider)
        verify(memberRepository).save(any(Member::class.java))
    }

    @Test
    @DisplayName("기존 OAuth 회원을 조회한다")
    fun findOrRegisterOauthMember_ExistingUser_ReturnsExisting() {
        // given
        val userInfo = UserInfo("kakao456", "existing@example.com")
        val provider = OauthProvider.KAKAO
        val existingMember = MemberFixture.onboardedMember()

        given(memberRepository.findByOauthInfo("kakao456", provider)).willReturn(existingMember)

        // when
        val result = memberService.findOrRegisterOauthMember(userInfo, provider)

        // then
        assertThat(result).isEqualTo(existingMember)
        verify(memberRepository).findByOauthInfo("kakao456", provider)
    }

    @Test
    @DisplayName("회원의 온보딩 정보를 업데이트한다")
    fun updateOnboardingInfo_ValidInput_UpdatesSuccessfully() {
        // given
        val member = MemberFixture.newMember()
        val command = OnboardingCommand(
            30, "SOUND", true, 5, 3,
            "BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5,
        )

        // when
        val result = memberService.updateOnboardingInfo(member, command)

        // then
        assertThat(result).isEqualTo(member)
        assertThat(result.preparationTime).isEqualTo(30)
        assertThat(result.isNewMember()).isFalse()
    }

    @Test
    @DisplayName("회원을 삭제한다")
    fun deleteMember_ValidMemberId_DeletesSuccessfully() {
        // given
        val memberId = 1L

        // when
        memberService.deleteMember(memberId)

        // then
        verify(memberRepository).deleteById(memberId)
    }
}
