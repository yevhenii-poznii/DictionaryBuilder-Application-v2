package com.kiskee.vocabulary.service.authentication;

import com.kiskee.vocabulary.config.properties.jwt.JwtProperties;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationData;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationResponse;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.model.dto.token.TokenData;
import com.kiskee.vocabulary.model.entity.token.CookieToken;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import com.kiskee.vocabulary.service.token.jwt.CookieTokenIssuer;
import com.kiskee.vocabulary.service.token.jwt.DefaultJweTokenFactory;
import com.kiskee.vocabulary.service.token.jwt.JweStringSerializer;
import com.kiskee.vocabulary.util.IdentityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

@Slf4j
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final DefaultJweTokenFactory defaultJweTokenFactory;
    private final JweStringSerializer tokenStringSerializer;
    private final CookieTokenIssuer cookieTokenIssuer;
    private final JwtProperties jwtProperties;

    @Override
    public AuthenticationResponse issueAccessToken() {
        Authentication authentication = IdentityUtil.getAuthentication();

        return issueAccessToken(authentication);
    }

    @Override
    public AuthenticationResponse issueAccessToken(String refreshToken) {
        Authentication authentication = IdentityUtil.getAuthentication();

        CookieToken cookieToken = cookieTokenIssuer.findTokenOrThrow(refreshToken);

        validate(cookieToken, authentication);

        return issueAccessToken(authentication);
    }

    @Override
    public TokenData issueRefreshToken(Authentication authentication) {
        TokenData tokenData = buildToken(authentication, jwtProperties.getRefreshExpirationTime());

        cookieTokenIssuer.persistToken(tokenData);

        log.info("Issued refresh token for user: [{}]", ((UserSecureProjection) authentication.getPrincipal()).getId());

        return tokenData;
    }

    private void validate(CookieToken cookieToken, Authentication authentication) {
        if (!cookieToken.getUserId().equals(((UserSecureProjection) authentication.getPrincipal()).getId())) {
            throw new CookieTheftException("Refresh token does not belong to the user");
        }

        if (cookieToken.isInvalidated()) {
            throw new InvalidCookieException("Refresh token is invalidated");
        }
    }

    private AuthenticationResponse issueAccessToken(Authentication authentication) {
        TokenData tokenData = buildToken(authentication, jwtProperties.getAccessExpirationTime());

        log.info("Issued access token for user: [{}]", ((UserSecureProjection) authentication.getPrincipal()).getId());

        return new AuthenticationResponse(
                tokenData.token(), tokenData.jweToken().getExpiresAt());
    }

    private TokenData buildToken(Authentication authentication, long expirationTime) {
        AuthenticationData authenticationData = new AuthenticationData(authentication, expirationTime);

        JweToken jweToken = defaultJweTokenFactory.apply(authenticationData);

        String tokenString = tokenStringSerializer.apply(jweToken);

        return new TokenData(tokenString, jweToken);
    }
}
