package com.kiskee.dictionarybuilder.service.token.jwt;

import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationData;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.repository.user.projections.UserSecureProjection;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class DefaultJweTokenFactory implements Function<AuthenticationData, JweToken> {

    @Override
    public JweToken apply(AuthenticationData authenticationData) {
        Instant now = Instant.now();
        Duration tokenTtl = Duration.ofSeconds(authenticationData.tokenTtl());
        List<String> authorities = mapAuthenticationToAuthorities(authenticationData.authentication());
        return JweToken.builder()
                .setUserId(((UserSecureProjection)
                                authenticationData.authentication().getPrincipal())
                        .getId())
                .setSubject(authenticationData.authentication().getName())
                .setAuthorities(authorities)
                .setCreatedAt(now)
                .setExpiresAt(now.plus(tokenTtl))
                .build();
    }

    private List<String> mapAuthenticationToAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
