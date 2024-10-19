package com.kiskee.dictionarybuilder.service.authentication;

import com.kiskee.dictionarybuilder.config.properties.token.jwt.JwtProperties;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationData;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationResponse;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.CookieToken;
import com.kiskee.dictionarybuilder.repository.user.projections.UserSecureProjection;
import com.kiskee.dictionarybuilder.service.security.token.serializer.TokenSerializer;
import com.kiskee.dictionarybuilder.service.token.jwt.CookieTokenIssuer;
import com.kiskee.dictionarybuilder.service.token.jwt.DefaultJweTokenFactory;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

@Slf4j
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService, LogoutService {

    private final DefaultJweTokenFactory defaultJweTokenFactory;
    private final TokenSerializer<JweToken, String> jweStringSerializer;
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
    public JweTokenData issueRefreshToken(Authentication authentication) {
        JweTokenData tokenData = buildToken(authentication, jwtProperties.getRefreshExpirationTime());
        cookieTokenIssuer.persistToken(tokenData);
        log.info("Issued refresh token for user: [{}]", ((UserSecureProjection) authentication.getPrincipal()).getId());
        return tokenData;
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        CookieToken cookieToken = cookieTokenIssuer.findTokenOrThrow(refreshToken);
        validate(cookieToken, IdentityUtil.getAuthentication());
        cookieTokenIssuer.invalidateToken(cookieToken.getToken());
        log.info("Revoked refresh token for user: [{}]", cookieToken.getUserId());
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
        JweTokenData tokenData = buildToken(authentication, jwtProperties.getAccessExpirationTime());
        log.info("Issued access token for user: [{}]", ((UserSecureProjection) authentication.getPrincipal()).getId());
        return new AuthenticationResponse(
                tokenData.token(), tokenData.jweToken().getExpiresAt());
    }

    private JweTokenData buildToken(Authentication authentication, long expirationTime) {
        AuthenticationData authenticationData = new AuthenticationData(authentication, expirationTime);
        JweToken jweToken = defaultJweTokenFactory.apply(authenticationData);
        String tokenString = jweStringSerializer.serialize(jweToken);
        return new JweTokenData(tokenString, jweToken);
    }
}
