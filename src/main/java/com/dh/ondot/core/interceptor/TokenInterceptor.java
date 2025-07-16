package com.dh.ondot.core.interceptor;

import com.dh.ondot.member.application.TokenFacade;
import com.dh.ondot.member.core.exception.InvalidTokenHeaderException;
import com.dh.ondot.member.core.exception.TokenMissingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenFacade tokenFacade;

    public TokenInterceptor(TokenFacade tokenFacade) {
        this.tokenFacade = tokenFacade;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            String jwtToken = request.getHeader(AUTHORIZATION_HEADER);
            String accessToken;
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new TokenMissingException();
            }

            if (jwtToken.startsWith(BEARER_PREFIX)) {
                accessToken = jwtToken.substring(BEARER_PREFIX.length());
            } else {
                throw new InvalidTokenHeaderException();
            }

            Long memberId = tokenFacade.validateToken(accessToken);
            request.setAttribute("memberId", memberId);
        }

        return true;
    }
}
