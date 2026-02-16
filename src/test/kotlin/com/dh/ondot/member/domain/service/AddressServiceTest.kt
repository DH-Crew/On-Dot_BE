package com.dh.ondot.member.domain.service

import com.dh.ondot.member.application.command.CreateAddressCommand
import com.dh.ondot.member.core.exception.NotFoundHomeAddressException
import com.dh.ondot.member.domain.Address
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.AddressType
import com.dh.ondot.member.domain.repository.AddressRepository
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

@ExtendWith(MockitoExtension::class)
@DisplayName("AddressService 테스트")
class AddressServiceTest {

    @Mock
    private lateinit var addressRepository: AddressRepository

    @InjectMocks
    private lateinit var addressService: AddressService

    @Test
    @DisplayName("홈 주소를 생성한다")
    fun createHomeAddress_ValidInput_CreatesAddress() {
        // given
        val member = MemberFixture.defaultMember()
        val command = CreateAddressCommand("서울시 강남구", 127.027619, 37.497952)
        val expectedAddress = Address.createByOnboarding(member, "서울시 강남구", 127.027619, 37.497952)

        given(addressRepository.save(any(Address::class.java))).willReturn(expectedAddress)

        // when
        val result = addressService.createHomeAddress(member, command)

        // then
        assertThat(result).isNotNull()
        assertThat(result.roadAddress).isEqualTo("서울시 강남구")
        assertThat(result.type).isEqualTo(AddressType.HOME)
        verify(addressRepository).save(any(Address::class.java))
    }

    @Test
    @DisplayName("회원 ID로 홈 주소를 조회한다")
    fun getHomeAddress_ValidMemberId_ReturnsAddress() {
        // given
        val memberId = 1L
        val member = MemberFixture.memberWithId(memberId)
        val address = Address.createByOnboarding(member, "서울시 강남구", 127.027619, 37.497952)

        given(addressRepository.findByMemberIdAndType(memberId, AddressType.HOME))
            .willReturn(address)

        // when
        val result = addressService.getHomeAddress(memberId)

        // then
        assertThat(result).isNotNull()
        assertThat(result.roadAddress).isEqualTo("서울시 강남구")
        assertThat(result.type).isEqualTo(AddressType.HOME)
        verify(addressRepository).findByMemberIdAndType(memberId, AddressType.HOME)
    }

    @Test
    @DisplayName("존재하지 않는 회원의 홈 주소 조회 시 예외가 발생한다")
    fun getHomeAddress_InvalidMemberId_ThrowsException() {
        // given
        val memberId = 999L

        given(addressRepository.findByMemberIdAndType(memberId, AddressType.HOME))
            .willReturn(null)

        // when & then
        assertThatThrownBy { addressService.getHomeAddress(memberId) }
            .isInstanceOf(NotFoundHomeAddressException::class.java)

        verify(addressRepository).findByMemberIdAndType(memberId, AddressType.HOME)
    }

    @Test
    @DisplayName("홈 주소를 업데이트한다")
    fun updateHomeAddress_ValidInput_UpdatesAddress() {
        // given
        val memberId = 1L
        val member = MemberFixture.memberWithId(memberId)
        val existingAddress = Address.createByOnboarding(member, "기존 주소", 126.0, 37.0)
        val command = CreateAddressCommand("새로운 주소", 127.027619, 37.497952)

        given(addressRepository.findByMemberIdAndType(memberId, AddressType.HOME))
            .willReturn(existingAddress)

        // when
        val result = addressService.updateHomeAddress(memberId, command)

        // then
        assertThat(result.roadAddress).isEqualTo("새로운 주소")
        assertThat(result.longitude).isEqualTo(127.027619)
        assertThat(result.latitude).isEqualTo(37.497952)
    }

    @Test
    @DisplayName("회원 ID로 모든 주소를 삭제한다")
    fun deleteAllByMemberId_ValidMemberId_DeletesAddresses() {
        // given
        val memberId = 1L

        // when
        addressService.deleteAllByMemberId(memberId)

        // then
        verify(addressRepository).deleteByMemberId(memberId)
    }
}
