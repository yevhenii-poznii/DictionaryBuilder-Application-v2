package com.kiskee.dictionarybuilder.service.token;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTokenService {

    protected abstract TokenRepository getTokenRepository();

    protected abstract Token buildToken(TokenData tokenData, String tokenString);

    protected String saveToken(TokenData tokenData, String tokenString) {
        Token token = buildToken(tokenData, tokenString);
        getTokenRepository().save(token);
        log.info("[{}] has been saved for [{}]", token.getClass().getSimpleName(), token.getUserId());
        return tokenString;
    }

    public void invalidateToken(String token) {
        getTokenRepository().invalidateToken(token);
    }

    public boolean isNotInvalidated(String token) {
        return getTokenRepository().existsByTokenAndIsInvalidatedFalse(token);
    }
}
