package com.kiskee.dictionarybuilder.web.auth;

import com.kiskee.dictionarybuilder.model.dto.registration.OAuth2ProvisionRequest;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.OAuth2ProvisionData;
import com.kiskee.dictionarybuilder.service.provision.oauth.OAuth2UserProvisionService;
import com.kiskee.dictionarybuilder.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final OAuth2UserProvisionService oAuth2UserProvisionService;

    private static final String REDIRECT_URI_PARAM = "&redirect_uri=";
    private static final String TOKEN_PARAM = "?token=";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User principal = oauthToken.getPrincipal();
            OAuth2ProvisionRequest userProvisionRequest = buildProvisionRequest(principal);
            OAuth2ProvisionData provisionData = oAuth2UserProvisionService.provisionUser(userProvisionRequest);

            Cookie cookie = CookieUtil.buildCookie(provisionData.refreshToken());
            Cookie jsessionid = CookieUtil.removeJsessionIdCookie();
            response.addCookie(cookie);
            response.addCookie(jsessionid);
            log.info("Cookie set successfully set for user");

            String state = request.getParameter("state");
            String redirectUri = null;
            if (StringUtils.hasText(state) && state.contains(REDIRECT_URI_PARAM)) {
                redirectUri = state.substring(state.indexOf(REDIRECT_URI_PARAM) + REDIRECT_URI_PARAM.length());
            }
            if (redirectUri != null) {
                response.sendRedirect(redirectUri + TOKEN_PARAM + provisionData.accessToken());
            } else {
                super.onAuthenticationSuccess(request, response, authentication);
            }
        }
    }

    private OAuth2ProvisionRequest buildProvisionRequest(OAuth2User principal) {
        String email = principal.getAttribute("email");
        String username = Objects.requireNonNull(email).substring(0, email.indexOf("@"));
        String name = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");
        List<? extends GrantedAuthority> authorities = principal.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().equalsIgnoreCase("OAUTH2_USER"))
                .toList();
        return new OAuth2ProvisionRequest(email, username, name, picture, authorities);
    }
}
