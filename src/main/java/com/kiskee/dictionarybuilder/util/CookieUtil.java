package com.kiskee.dictionarybuilder.util;

import com.kiskee.dictionarybuilder.exception.ForbiddenAccessException;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CookieUtil {

    public static final String COOKIE_NAME = "RefreshAuthentication";

    public Cookie buildCookie(JweTokenData tokenData) {
        Cookie cookie = new Cookie(COOKIE_NAME, tokenData.token());
        cookie.setPath("/api/v1");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge((int)
                ChronoUnit.SECONDS.between(Instant.now(), tokenData.jweToken().getExpiresAt()));

        return cookie;
    }

    public Cookie removeJsessionIdCookie() {
        Cookie jsessionid = new Cookie("JSESSIONID", null);
        jsessionid.setMaxAge(0);
        jsessionid.setPath("/api/v1");
        return jsessionid;
    }

    public String extractTokenFromCookie(Cookie[] cookies) {
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(COOKIE_NAME))
                    .findFirst()
                    .orElseThrow(() -> new ForbiddenAccessException("Cookie token not found"))
                    .getValue();
        }

        return null;
    }
}
