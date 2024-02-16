package com.kiskee.vocabulary.util;

import com.kiskee.vocabulary.model.dto.token.TokenData;
import jakarta.servlet.http.Cookie;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@UtilityClass
public class CookieUtil {

    public Cookie buildCookie(TokenData tokenData) {
        Cookie cookie = new Cookie("RefreshAuthentication", tokenData.token());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge((int) ChronoUnit.SECONDS.between(Instant.now(), tokenData.jweToken().getExpiresAt()));

        return cookie;
    }

    public String extractTokenFromCookie(Cookie[] cookies) {
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals("RefreshAuthentication"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"))
                    .getValue();
        }

        return null;
    }

}
