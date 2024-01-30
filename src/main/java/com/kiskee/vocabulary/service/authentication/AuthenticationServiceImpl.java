package com.kiskee.vocabulary.service.authentication;

import com.kiskee.vocabulary.config.properties.jwt.JwtProperties;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationResponse;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import com.kiskee.vocabulary.model.dto.authentication.AuthenticationData;
import com.kiskee.vocabulary.service.token.jwt.DefaultJweTokenFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Slf4j
@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final DefaultJweTokenFactory defaultJweTokenFactory;
    private final Function<JweToken, String> tokenStringSerializer;
    private final JwtProperties jwtProperties;


    @Override
    public AuthenticationResponse issueAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AuthenticationData authenticationData = new AuthenticationData(authentication,
                jwtProperties.getAccessExpirationTime());

        JweToken jweToken = defaultJweTokenFactory.apply(authenticationData);

        String tokenString = tokenStringSerializer.apply(jweToken);

        log.info("Issued access token for user: [{}]", ((UserSecureProjection) authentication.getPrincipal()).getId());

        return new AuthenticationResponse(tokenString);
    }

}
