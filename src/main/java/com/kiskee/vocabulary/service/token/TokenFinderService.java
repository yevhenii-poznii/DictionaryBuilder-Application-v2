package com.kiskee.vocabulary.service.token;

import com.kiskee.vocabulary.model.entity.token.Token;

public interface TokenFinderService<T extends Token> {

    T findTokenOrThrow(String token);
}
