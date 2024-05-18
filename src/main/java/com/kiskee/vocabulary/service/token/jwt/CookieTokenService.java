package com.kiskee.vocabulary.service.token.jwt;

import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.model.dto.token.TokenData;
import com.kiskee.vocabulary.model.entity.token.CookieToken;
import com.kiskee.vocabulary.model.entity.token.Token;
import com.kiskee.vocabulary.repository.token.TokenRepository;
import com.kiskee.vocabulary.service.token.AbstractTokenService;
import com.kiskee.vocabulary.service.token.TokenInvalidatorService;
import com.kiskee.vocabulary.service.token.TokenPersistenceService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Getter
@Service
@AllArgsConstructor
public class CookieTokenService extends AbstractTokenService<JweToken, CookieToken>
        implements TokenPersistenceService<TokenData, String>, TokenInvalidatorService<CookieToken>, CookieTokenIssuer {

    private final TokenRepository tokenRepository;

    @Override
    public String persistToken(TokenData tokenData) {
        saveToken(tokenData.jweToken(), tokenData.token());

        return tokenData.token();
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
