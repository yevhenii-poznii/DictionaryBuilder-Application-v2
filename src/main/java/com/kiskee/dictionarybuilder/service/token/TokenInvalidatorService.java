package com.kiskee.dictionarybuilder.service.token;

import com.kiskee.dictionarybuilder.model.entity.token.Token;

public interface TokenInvalidatorService<T extends Token> extends TokenDataClassProvider {

    boolean isNotInvalidated(String token);

    void invalidateToken(String token);
}
