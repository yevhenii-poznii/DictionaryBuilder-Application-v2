package com.kiskee.dictionarybuilder.service.token.jwt;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import com.kiskee.dictionarybuilder.model.entity.token.CookieToken;
import com.kiskee.dictionarybuilder.service.token.TokenFinderService;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;

public interface CookieTokenIssuer
        extends TokenPersistenceService<TokenData, String>, TokenFinderService<CookieToken> {}
