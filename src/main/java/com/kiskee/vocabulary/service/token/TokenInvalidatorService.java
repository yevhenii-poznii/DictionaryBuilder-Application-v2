package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.model.entity.token.Token;

public interface TokenInvalidatorService<T extends Token> {

    T findTokenOrThrow(String token);

    void invalidateToken(T token);

}
