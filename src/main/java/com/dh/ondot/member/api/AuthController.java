package com.dh.ondot.member.api;

import com.dh.ondot.member.api.response.AccessToken;
import com.dh.ondot.member.api.response.Token;
import com.dh.ondot.member.app.AuthFacade;
import com.dh.ondot.member.core.exception.TokenMissingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthFacade authFacade;

    @PostMapping("/kakao-login")
    public Token kakaoCallback(
            @RequestBody Map<String, String> request
    ) {
        String code = request.get("code");

        return authFacade.kakaoLogin(code);
    }

    @PostMapping("/reissue")
    public Token reissue(
            HttpServletRequest request
    ) {
        String refreshToken = extractRefreshToken(request);

        return authFacade.reissueToken(refreshToken);
    }

    @PostMapping("/test/token")
    public AccessToken testToken() {

        return authFacade.issueTokenForTest();
    }

    private String extractRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new TokenMissingException();
        }
        return bearerToken.substring(7);
    }
}