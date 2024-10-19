package com.kiskee.dictionarybuilder.service.security.token.deserializer;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;

public interface TokenDeserializer<S, D extends TokenData> {

    D deserialize(S string) throws Exception;
}
