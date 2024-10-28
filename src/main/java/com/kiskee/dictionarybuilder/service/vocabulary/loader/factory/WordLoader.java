package com.kiskee.dictionarybuilder.service.vocabulary.loader.factory;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.WordFilter;

public interface WordLoader<F extends WordFilter, Request, Response> {

    F getFilter();

    Response loadWords(Long dictionaryId, Request request);
}
