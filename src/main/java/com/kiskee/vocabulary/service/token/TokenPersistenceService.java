package com.kiskee.vocabulary.service.token;

public interface TokenPersistenceService<T, S> {

    S persistToken(T t);

}
