package com.kiskee.dictionarybuilder.service.token;

public interface TokenPersistenceService<T, S> {

    S persistToken(T t);
}
