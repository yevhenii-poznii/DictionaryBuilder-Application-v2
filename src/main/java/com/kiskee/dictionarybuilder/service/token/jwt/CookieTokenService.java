package com.kiskee.dictionarybuilder.service.token.jwt;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.CookieToken;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.service.token.AbstractTokenService;
import com.kiskee.dictionarybuilder.util.ThrowUtil;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Getter
@Service
@AllArgsConstructor
public class CookieTokenService extends AbstractTokenService implements CookieTokenIssuer {

    private final TokenRepository tokenRepository;

    @Override
    public CookieToken findTokenOrThrow(String tokenString) {
        return (CookieToken) tokenRepository
                .findByToken(tokenString)
                .orElseThrow(ThrowUtil.throwNotFoundException(Token.class.getSimpleName(), tokenString));
    }

    @Override
    public String persistToken(JweTokenData tokenData) {
        return saveToken(tokenData, tokenData.token());
    }

    @Override
    protected Token buildToken(TokenData tokenData, String tokenString) {
        return new CookieToken(tokenString, tokenData.getUserId(), Instant.now(), tokenData.getExpiresAt());
    }

    @Override
    public Class<? extends TokenData> getSupportedTokenDataClass() {
        return JweToken.class;
    }
}
