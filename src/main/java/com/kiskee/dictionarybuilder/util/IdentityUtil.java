package com.kiskee.dictionarybuilder.util;

import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.user.projections.UserSecureProjection;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class IdentityUtil {

    private final SimpleGrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");
    private final SimpleGrantedAuthority OAUTH2_USER = new SimpleGrantedAuthority("OAUTH2_USER");

    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserId(authentication);
    }

    public UUID getUserId(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return ((UserSecureProjection) authentication.getPrincipal()).getId();
        }

        return null;
    }

    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated(authentication)) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }

        return authentication;
    }

    public void setAuthentication(JweToken jweToken) {
        UserVocabularyApplication user = UserVocabularyApplication.builder()
                .setId(jweToken.getUserId())
                .setUsername(jweToken.getSubject())
                .build();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user,
                null,
                jweToken.getAuthorities().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));

        setAuthentication(token);
    }

    public void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return !Objects.isNull(authentication) && hasUserRole(authentication.getAuthorities());
    }

    private boolean hasUserRole(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(ROLE_USER) || authorities.contains(OAUTH2_USER);
    }
}
