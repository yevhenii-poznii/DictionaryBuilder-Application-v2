package com.kiskee.vocabulary.service.authentication;

import com.kiskee.vocabulary.config.properties.jwt.JwtProperties;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationData;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationResponse;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.model.entity.token.CookieToken;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import com.kiskee.vocabulary.service.token.TokenFinderService;
import com.kiskee.vocabulary.service.token.jwt.DefaultJweTokenFactory;
import com.kiskee.vocabulary.service.token.jwt.JweStringSerializer;
import com.kiskee.vocabulary.util.IdentityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final DefaultJweTokenFactory defaultJweTokenFactory;
    private final JweStringSerializer tokenStringSerializer;
    private final TokenFinderService<CookieToken> tokenFinderService;
    private final JwtProperties jwtProperties;

    @Override
    public AuthenticationResponse issueAccessToken() {
        Authentication authentication = IdentityUtil.getAuthentication();

        return buildAccessToken(authentication);
    }

    @Override
    public AuthenticationResponse issueAccessToken(String refreshToken) {
        Authentication authentication = IdentityUtil.getAuthentication();

        CookieToken cookieToken = tokenFinderService.findTokenOrThrow(refreshToken);

        validate(cookieToken, authentication);

        return buildAccessToken(authentication);
    }

    private void validate(CookieToken cookieToken, Authentication authentication) {
        if (!cookieToken.getUserId().equals(((UserSecureProjection) authentication.getPrincipal()).getId())) {
            throw new CookieTheftException("Refresh token does not belong to the user");
        }

        if (cookieToken.isInvalidated()) {
            throw new InvalidCookieException("Refresh token is invalidated");
        }
    }

    private AuthenticationResponse buildAccessToken(Authentication authentication) {
        AuthenticationData authenticationData = new AuthenticationData(authentication,
                jwtProperties.getAccessExpirationTime());

        JweToken jweToken = defaultJweTokenFactory.apply(authenticationData);

        String tokenString = tokenStringSerializer.apply(jweToken);

        log.info("Issued access token for user: [{}]", ((UserSecureProjection) authentication.getPrincipal()).getId());

        return new AuthenticationResponse(tokenString, jweToken.getExpiresAt());
    }

}
