package com.dh.ondot.core.interceptor;

import com.dh.ondot.member.app.TokenFacade;
import com.dh.ondot.member.core.exception.InvalidTokenHeaderException;
import com.dh.ondot.member.core.exception.TokenMissingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final TokenFacade tokenFacade;

    @Autowired
    public TokenInterceptor(TokenFacade tokenFacade) {
        this.tokenFacade = tokenFacade;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {

            String jwtToken = request.getHeader("Authorization");
            String accessToken;
            if (jwtToken == null) {
                throw new TokenMissingException();
            }

            if (jwtToken.startsWith("Bearer ")) {
                accessToken = jwtToken.substring(7);
            } else {
                throw new InvalidTokenHeaderException();
            }

            Long memberId = tokenFacade.validateToken(accessToken);

            request.setAttribute("memberId", memberId);
        }

        return true;
    }
}
