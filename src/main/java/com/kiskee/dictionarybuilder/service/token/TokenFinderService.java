package com.kiskee.dictionarybuilder.service.token;

import com.kiskee.dictionarybuilder.model.entity.token.Token;

public interface TokenFinderService<T extends Token> {

    T findTokenOrThrow(String token);
}
