package com.dh.ondot.member.api;

import com.dh.ondot.member.api.response.AccessToken;
import com.dh.ondot.member.app.dto.Token;
import com.dh.ondot.member.app.AuthFacade;
import com.dh.ondot.member.app.TokenFacade;
import com.dh.ondot.member.core.exception.TokenMissingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthFacade authFacade;
    private final TokenFacade tokenFacade;

    @PostMapping("/login/oauth")
    public Token loginWithOAuth(
            @RequestParam("oauth_provider") String oauthProvider,
            @RequestParam("access_token") String accessToken
    ) {
        return authFacade.loginWithOAuth(oauthProvider, accessToken);
    }

    @PostMapping("/reissue")
    public Token reissue(
            HttpServletRequest request
    ) {
        String refreshToken = extractRefreshToken(request);

        return tokenFacade.reissue(refreshToken);
    }

    @PostMapping("/test/token")
    public AccessToken testToken() {
        Token token = tokenFacade.issue(1L);
        return new AccessToken(token.accessToken());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new TokenMissingException();
        }
        return bearerToken.substring(7);
    }
}
