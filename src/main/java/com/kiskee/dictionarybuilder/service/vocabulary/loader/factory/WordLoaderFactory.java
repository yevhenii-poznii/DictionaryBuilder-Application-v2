package com.kiskee.dictionarybuilder.service.vocabulary.loader.factory;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.WordFilter;

public interface WordLoaderFactory<F extends WordFilter, L extends WordLoader<F, ?, ?>> {

    L getLoader(F filter);
}
