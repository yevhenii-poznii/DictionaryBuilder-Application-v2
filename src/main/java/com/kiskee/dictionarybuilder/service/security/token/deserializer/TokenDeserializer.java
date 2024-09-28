package com.kiskee.dictionarybuilder.service.security.token.deserializer;

public interface TokenDeserializer<S, D> {

    D deserialize(S string, Class<D> cls);
}
