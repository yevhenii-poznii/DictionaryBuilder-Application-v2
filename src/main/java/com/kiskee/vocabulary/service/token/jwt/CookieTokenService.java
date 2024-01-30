package com.kiskee.vocabulary.service.token.jwt;

import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.model.entity.token.CookieToken;
import com.kiskee.vocabulary.model.entity.token.Token;
import com.kiskee.vocabulary.repository.token.TokenRepository;
import com.kiskee.vocabulary.service.token.AbstractTokenService;
import com.kiskee.vocabulary.service.token.TokenGeneratorService;
import com.kiskee.vocabulary.service.token.TokenInvalidatorService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
@AllArgsConstructor
public class CookieTokenService extends AbstractTokenService<JweToken, CookieToken>
        implements TokenGeneratorService<JweToken, String>, TokenInvalidatorService<CookieToken> {

    @Getter
    private final TokenRepository tokenRepository;
    private final Function<JweToken, String> tokenStringSerializer;

    @Override
    public String generateToken(JweToken jweToken) {
        String cookieToken = tokenStringSerializer.apply(jweToken);

        saveToken(jweToken, cookieToken);

        return cookieToken;
    }

    @Override
    public CookieToken findTokenOrThrow(String tokenString) {
        return (CookieToken) super.findTokenOrThrow(tokenString);
    }

    @Override
    public void invalidateToken(CookieToken token) {
        super.invalidateToken(token);
    }

    @Override
    protected Token buildToken(JweToken jweToken, String tokenString) {
        return new CookieToken(tokenString, jweToken.getId(), jweToken.getCreatedAt(), jweToken.getExpiresAt());
    }

}
