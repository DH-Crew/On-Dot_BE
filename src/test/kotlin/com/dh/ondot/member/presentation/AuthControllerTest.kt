package com.dh.ondot.member.presentation

import com.dh.ondot.member.application.AuthFacade
import com.dh.ondot.member.application.TokenFacade
import com.dh.ondot.member.application.dto.LoginResult
import com.dh.ondot.member.application.dto.Token
import com.dh.ondot.member.core.OauthProviderConverter
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.presentation.response.AccessToken
import com.dh.ondot.member.presentation.response.LoginResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthController::class)
@Import(OauthProviderConverter::class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authFacade: AuthFacade

    @MockitoBean
    private lateinit var tokenFacade: TokenFacade

    @Test
    @DisplayName("OAuth 로그인 정상 요청 시 200과 LoginResponse를 반환한다")
    fun loginWithOAuth_success_200() {
        val result = LoginResult(1L, "access-token", "refresh-token", true)
        given(authFacade.loginWithOAuth(OauthProvider.KAKAO, "oauth-token")).willReturn(result)

        mockMvc.perform(
            post("/auth/login/oauth")
                .param("provider", "KAKAO")
                .param("access_token", "oauth-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.memberId").value(1))
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
            .andExpect(jsonPath("$.isNewMember").value(true))
    }

    @Test
    @DisplayName("OAuth 로그인 시 provider 누락하면 400을 반환한다")
    fun loginWithOAuth_missingProvider_400() {
        mockMvc.perform(
            post("/auth/login/oauth")
                .param("access_token", "oauth-token")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("OAuth 로그인 시 access_token 누락하면 400을 반환한다")
    fun loginWithOAuth_missingAccessToken_400() {
        mockMvc.perform(
            post("/auth/login/oauth")
                .param("provider", "KAKAO")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("토큰 재발급 정상 요청 시 200과 Token을 반환한다")
    fun reissue_success_200() {
        val token = Token("new-access", "new-refresh")
        given(tokenFacade.reissue("some-refresh-token")).willReturn(token)

        mockMvc.perform(
            post("/auth/reissue")
                .header("Authorization", "Bearer some-refresh-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new-access"))
            .andExpect(jsonPath("$.refreshToken").value("new-refresh"))
    }

    @Test
    @DisplayName("토큰 재발급 시 Authorization 헤더 누락하면 400을 반환한다")
    fun reissue_missingAuthHeader_400() {
        mockMvc.perform(post("/auth/reissue"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errorCode").value("MISSING_REQUEST_HEADER"))
    }

    @Test
    @DisplayName("로그아웃 정상 요청 시 204를 반환한다")
    fun logout_success_204() {
        mockMvc.perform(
            post("/auth/logout")
                .header("Authorization", "Bearer some-refresh-token")
        )
            .andExpect(status().isNoContent)

        verify(tokenFacade).logoutByHeader("Bearer some-refresh-token")
    }

    @Test
    @DisplayName("로그아웃 시 잘못된 토큰 형식이어도 204를 반환한다")
    fun logout_invalidTokenFormat_204() {
        mockMvc.perform(
            post("/auth/logout")
                .header("Authorization", "InvalidFormat token")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    @DisplayName("테스트 토큰 발급 시 200과 AccessToken을 반환한다")
    fun testToken_success_200() {
        val token = Token("test-access-token", "test-refresh-token")
        given(tokenFacade.issue(1L)).willReturn(token)

        mockMvc.perform(post("/auth/test/token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("test-access-token"))
    }
}
