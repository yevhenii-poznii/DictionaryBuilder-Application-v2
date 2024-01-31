package com.kiskee.vocabulary.service.token;

public interface TokenGeneratorService<T, S> {

    S generateToken(T t);

}
