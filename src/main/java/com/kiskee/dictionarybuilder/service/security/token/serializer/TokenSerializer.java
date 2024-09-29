package com.kiskee.dictionarybuilder.service.security.token.serializer;

public interface TokenSerializer<D, S> {

    S serialize(D data);
}
