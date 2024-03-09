package com.kiskee.vocabulary.service.vocabulary.dictionary.page.impl;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.service.vocabulary.dictionary.page.DictionaryPageLoaderFactory;
import com.kiskee.vocabulary.service.vocabulary.word.page.DictionaryPageLoader;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DictionaryPageLoaderFactoryImpl implements DictionaryPageLoaderFactory {

    private final Map<String, DictionaryPageLoader> loaders;

    public DictionaryPageLoaderFactoryImpl(List<DictionaryPageLoader> dictionaryPageLoaders) {
        this.loaders = new HashMap<>();
        dictionaryPageLoaders
                .forEach(loader -> loaders.put(loader.getPageFilter().getFilter(), loader));
    }

    public DictionaryPageLoader getLoader(PageFilter filter) {
        return loaders.get(filter.getFilter());
    }

}
