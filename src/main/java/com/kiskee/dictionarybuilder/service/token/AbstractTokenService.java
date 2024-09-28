package com.kiskee.dictionarybuilder.service.token;

import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.util.ThrowUtil;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTokenService<T, S extends Token> {

    protected abstract TokenRepository getTokenRepository();

    protected abstract Token buildToken(T tokenData, String tokenString);

    protected void saveToken(T tokenData, String tokenString) {
        Token token = buildToken(tokenData, tokenString);
        getTokenRepository().save(token);
        log.info("[{}] has been saved for [{}]", token.getClass().getSimpleName(), token.getUserId());
    }

    protected Token findTokenOrThrow(String tokenString) {
        return getTokenRepository()
                .findByToken(tokenString)
                .orElseThrow(ThrowUtil.throwNotFoundException(Token.class.getSimpleName(), tokenString));
    }

    protected void invalidateToken(S token) {
        token.setInvalidated(true);
        token.setExpiresAt(Instant.now());
        getTokenRepository().save(token);
        log.info(
                "[{}] for user [{}] has been successfully invalidated",
                token.getClass().getSimpleName(),
                token.getUserId());
    }
}
