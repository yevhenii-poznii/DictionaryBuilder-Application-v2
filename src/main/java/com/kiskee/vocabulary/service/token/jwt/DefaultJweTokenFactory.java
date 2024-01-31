package com.kiskee.vocabulary.service.token.jwt;

import com.kiskee.vocabulary.model.dto.authentication.AuthenticationData;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

@Component
public class DefaultJweTokenFactory implements Function<AuthenticationData, JweToken> {

    @Override
    public JweToken apply(AuthenticationData authenticationData) {
        Instant now = Instant.now();

        Duration tokenTtl = Duration.ofSeconds(authenticationData.tokenTtl());

        List<String> authorities = mapAuthenticationToAuthorities(authenticationData.authentication());

        return JweToken.builder()
                .setId(((UserSecureProjection) authenticationData.authentication().getPrincipal()).getId())
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
