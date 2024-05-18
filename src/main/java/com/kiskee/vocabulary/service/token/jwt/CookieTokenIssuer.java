package com.kiskee.vocabulary.service.token.jwt;

import com.kiskee.vocabulary.model.dto.token.TokenData;
import com.kiskee.vocabulary.model.entity.token.CookieToken;
import com.kiskee.vocabulary.service.token.TokenFinderService;
import com.kiskee.vocabulary.service.token.TokenPersistenceService;

public interface CookieTokenIssuer
        extends TokenPersistenceService<TokenData, String>, TokenFinderService<CookieToken> {}
