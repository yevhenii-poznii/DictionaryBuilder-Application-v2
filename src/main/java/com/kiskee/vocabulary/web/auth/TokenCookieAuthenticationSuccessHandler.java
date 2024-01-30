package com.kiskee.vocabulary.web.auth;

import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.service.token.TokenGeneratorService;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationData;
import com.kiskee.vocabulary.service.token.jwt.DefaultJweTokenFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@AllArgsConstructor
public class TokenCookieAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final DefaultJweTokenFactory defaultJweTokenFactory;
    private final TokenGeneratorService<JweToken, String> tokenGeneratorService;
    private final long refreshExpirationTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            AuthenticationData authenticationData = new AuthenticationData(authentication, refreshExpirationTime);

            JweToken token = defaultJweTokenFactory.apply(authenticationData);

            String tokenString = tokenGeneratorService.generateToken(token);

            Cookie cookie = buildCookie(tokenString, token);

            response.addCookie(cookie);
        }
    }

    private Cookie buildCookie(String tokenString, JweToken jweToken) {
        Cookie cookie = new Cookie("RefreshAuthentication", tokenString);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge((int) ChronoUnit.SECONDS.between(Instant.now(), jweToken.getExpiresAt()));

        return cookie;
    }

}
