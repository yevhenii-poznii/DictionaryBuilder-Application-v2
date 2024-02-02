package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.model.entity.token.Token;

public interface TokenInvalidatorService<T extends Token> extends TokenFinderService<T> {

    void invalidateToken(T token);

}
