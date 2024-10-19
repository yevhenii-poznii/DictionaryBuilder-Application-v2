package com.kiskee.dictionarybuilder.service.security.token.serializer;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;

public interface TokenSerializer<D extends TokenData, S> {

    S serialize(D data);
}
