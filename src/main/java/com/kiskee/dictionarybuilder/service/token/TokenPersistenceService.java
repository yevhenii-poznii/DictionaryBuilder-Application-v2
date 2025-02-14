package com.kiskee.dictionarybuilder.service.token;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;

public interface TokenPersistenceService<D extends TokenData> {

    String persistToken(D tokenData);
}
