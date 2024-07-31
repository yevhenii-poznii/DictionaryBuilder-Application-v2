package com.kiskee.dictionarybuilder.service.token;

import com.kiskee.dictionarybuilder.model.entity.token.Token;

public interface TokenInvalidatorService<T extends Token> extends TokenFinderService<T> {

    void invalidateToken(T token);
}
