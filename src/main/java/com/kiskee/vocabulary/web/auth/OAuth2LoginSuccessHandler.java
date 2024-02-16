package com.kiskee.vocabulary.web.auth;

import com.kiskee.vocabulary.model.dto.registration.OAuth2ProvisionRequest;
import com.kiskee.vocabulary.model.dto.token.OAuth2ProvisionData;
import com.kiskee.vocabulary.service.oauth.OAuth2UserProvisionService;
import com.kiskee.vocabulary.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final OAuth2UserProvisionService oAuth2UserProvisionService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User principal = oauthToken.getPrincipal();

            OAuth2ProvisionRequest userProvisionRequest = buildProvisionRequest(principal);

            OAuth2ProvisionData provisionData = oAuth2UserProvisionService.provisionUser(userProvisionRequest);

            String redirectUrl = String.format("http://localhost:3000/signin?token=%s", provisionData.accessToken());

            Cookie cookie = CookieUtil.buildCookie(provisionData.refreshToken());

            response.addCookie(cookie);
            response.sendRedirect(redirectUrl);

            log.info("Cookie set successfully set for user");
        }
    }

    private OAuth2ProvisionRequest buildProvisionRequest(OAuth2User principal) {
        String email = principal.getAttribute("email");
        String username = Objects.requireNonNull(email).substring(0, email.indexOf("@"));
        String name = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");

        return new OAuth2ProvisionRequest(email, username, name, picture);
    }

}
