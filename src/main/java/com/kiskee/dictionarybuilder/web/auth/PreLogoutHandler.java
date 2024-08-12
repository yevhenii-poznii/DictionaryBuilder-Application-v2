package com.kiskee.dictionarybuilder.web.auth;

import com.kiskee.dictionarybuilder.service.authentication.LogoutService;
import com.kiskee.dictionarybuilder.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@AllArgsConstructor
public class PreLogoutHandler implements LogoutHandler {

    private final LogoutService logoutService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutService.revokeRefreshToken(CookieUtil.extractTokenFromCookie(request.getCookies()));
    }
}
