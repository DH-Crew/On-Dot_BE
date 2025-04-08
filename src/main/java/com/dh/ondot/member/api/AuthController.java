package com.dh.ondot.member.api;

import com.dh.ondot.member.api.response.AccessToken;
import com.dh.ondot.member.app.dto.Token;
import com.dh.ondot.member.app.AuthFacade;
import com.dh.ondot.member.app.TokenFacade;
import com.dh.ondot.member.core.TokenExtractor;
import com.dh.ondot.member.core.exception.TokenMissingException;
import com.dh.ondot.member.domain.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthFacade authFacade;
    private final TokenFacade tokenFacade;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login/oauth")
    public Token loginWithOAuth(
            @RequestParam("provider") OauthProvider oauthProvider,
            @RequestParam("access_token") String accessToken
    ) {
        return authFacade.loginWithOAuth(oauthProvider, accessToken);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/reissue")
    public Token reissue(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        String refreshToken = TokenExtractor.extract(token);

        return tokenFacade.reissue(refreshToken);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public void logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        String refreshToken = TokenExtractor.extract(token);
        tokenFacade.logout(refreshToken);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/test/token")
    public AccessToken testToken() {
        Token token = tokenFacade.issue(1L);
        return new AccessToken(token.accessToken());
    }
}
