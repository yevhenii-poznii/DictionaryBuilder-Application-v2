package com.kiskee.dictionarybuilder.service.token.jwt;

import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.CookieToken;
import com.kiskee.dictionarybuilder.service.token.TokenFinderService;
import com.kiskee.dictionarybuilder.service.token.TokenInvalidatorService;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;

public interface CookieTokenIssuer
        extends TokenPersistenceService<JweTokenData>,
                TokenInvalidatorService<CookieToken>,
                TokenFinderService<CookieToken> {}
