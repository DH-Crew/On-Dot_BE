package com.dh.ondot.member.domain.service;

import com.dh.ondot.member.application.command.CreateAddressCommand;
import com.dh.ondot.member.core.exception.NotFoundAddressException;
import com.dh.ondot.member.domain.Address;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.enums.AddressType;
import com.dh.ondot.member.domain.repository.AddressRepository;
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
@DisplayName("AddressService 테스트")
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    @DisplayName("홈 주소를 생성한다")
    void createHomeAddress_ValidInput_CreatesAddress() {
        // given
        Member member = MemberFixture.defaultMember();
        CreateAddressCommand command = new CreateAddressCommand("서울시 강남구", 127.027619, 37.497952);
        Address expectedAddress = Address.createByOnboarding(member, "서울시 강남구", 127.027619, 37.497952);
        
        given(addressRepository.save(any(Address.class))).willReturn(expectedAddress);

        // when
        Address result = addressService.createHomeAddress(member, command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRoadAddress()).isEqualTo("서울시 강남구");
        assertThat(result.getType()).isEqualTo(AddressType.HOME);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("회원 ID로 홈 주소를 조회한다")
    void getHomeAddress_ValidMemberId_ReturnsAddress() {
        // given
        Long memberId = 1L;
        Member member = MemberFixture.memberWithId(memberId);
        Address address = Address.createByOnboarding(member, "서울시 강남구", 127.027619, 37.497952);
        
        given(addressRepository.findByMemberIdAndType(memberId, AddressType.HOME))
                .willReturn(Optional.of(address));

        // when
        Address result = addressService.getHomeAddress(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRoadAddress()).isEqualTo("서울시 강남구");
        assertThat(result.getType()).isEqualTo(AddressType.HOME);
        verify(addressRepository).findByMemberIdAndType(memberId, AddressType.HOME);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 홈 주소 조회 시 예외가 발생한다")
    void getHomeAddress_InvalidMemberId_ThrowsException() {
        // given
        Long memberId = 999L;
        
        given(addressRepository.findByMemberIdAndType(memberId, AddressType.HOME))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addressService.getHomeAddress(memberId))
                .isInstanceOf(NotFoundAddressException.class);
        
        verify(addressRepository).findByMemberIdAndType(memberId, AddressType.HOME);
    }

    @Test
    @DisplayName("홈 주소를 업데이트한다")
    void updateHomeAddress_ValidInput_UpdatesAddress() {
        // given
        Long memberId = 1L;
        Member member = MemberFixture.memberWithId(memberId);
        Address existingAddress = Address.createByOnboarding(member, "기존 주소", 126.0, 37.0);
        CreateAddressCommand command = new CreateAddressCommand("새로운 주소", 127.027619, 37.497952);
        
        given(addressRepository.findByMemberIdAndType(memberId, AddressType.HOME))
                .willReturn(Optional.of(existingAddress));

        // when
        Address result = addressService.updateHomeAddress(memberId, command);

        // then
        assertThat(result.getRoadAddress()).isEqualTo("새로운 주소");
        assertThat(result.getLongitude()).isEqualTo(127.027619);
        assertThat(result.getLatitude()).isEqualTo(37.497952);
    }

    @Test
    @DisplayName("회원 ID로 모든 주소를 삭제한다")
    void deleteAllByMemberId_ValidMemberId_DeletesAddresses() {
        // given
        Long memberId = 1L;

        // when
        addressService.deleteAllByMemberId(memberId);

        // then
        verify(addressRepository).deleteByMemberId(memberId);
    }
}
