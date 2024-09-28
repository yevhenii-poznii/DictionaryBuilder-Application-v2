package com.kiskee.dictionarybuilder.service.token.share;

import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;

public interface SharingTokenService extends TokenPersistenceService<SharingTokenData, String> {

    boolean isNotInvalidated(String token);

    void invalidateToken(String token);
}
