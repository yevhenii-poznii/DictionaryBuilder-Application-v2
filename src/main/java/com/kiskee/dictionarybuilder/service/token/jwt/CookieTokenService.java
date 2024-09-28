package com.kiskee.dictionarybuilder.service.token.jwt;

import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.TokenData;
import com.kiskee.dictionarybuilder.model.entity.token.CookieToken;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.service.token.AbstractTokenService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Getter
@Service
@AllArgsConstructor
public class CookieTokenService extends AbstractTokenService<JweToken, CookieToken> implements CookieTokenIssuer {

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
