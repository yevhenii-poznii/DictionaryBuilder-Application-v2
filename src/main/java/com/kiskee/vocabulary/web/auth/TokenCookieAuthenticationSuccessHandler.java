package com.kiskee.vocabulary.web.auth;

import com.kiskee.vocabulary.model.dto.token.TokenData;
import com.kiskee.vocabulary.service.authentication.AuthenticationService;
import com.kiskee.vocabulary.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Slf4j
@AllArgsConstructor
public class TokenCookieAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            TokenData tokenData = authenticationService.issueRefreshToken(authentication);

            Cookie cookie = CookieUtil.buildCookie(tokenData);

            response.addCookie(cookie);
        }
    }
}
