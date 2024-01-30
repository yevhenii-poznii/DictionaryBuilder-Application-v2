package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.model.entity.token.Token;
import com.kiskee.vocabulary.repository.token.TokenRepository;
import com.kiskee.vocabulary.util.ThrowUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Optional;

@Slf4j
public abstract class AbstractTokenService<T, S extends Token> {

    protected abstract TokenRepository getTokenRepository();

    protected abstract Token buildToken(T tokenData, String tokenString);

    public void saveToken(T tokenData, String tokenString) {
        Token token = buildToken(tokenData, tokenString);

        getTokenRepository().save(token);

        log.info("[{}] has been saved for [{}]", token.getClass().getSimpleName(), token.getUserId());
    }

    protected Token findTokenOrThrow(String tokenString) {
        Optional<Token> token = getTokenRepository().findByToken(tokenString);

        return token.orElseThrow(ThrowUtil.throwNotFoundException("Token", tokenString));
    }

    protected void invalidateToken(S token) {
        token.setInvalidated(true);
        token.setExpiresAt(Instant.now());

        getTokenRepository().save(token);

        log.info("[{}] [{}] for user [{}] has been successfully invalidated",
                token.getClass().getSimpleName(), token.getToken(), token.getUserId());
    }

}
