package com.kiskee.dictionarybuilder.service.vocabulary.dictionary.page.impl;

import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.page.DictionaryPageLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DictionaryPageLoaderFactoryImpl implements DictionaryPageLoaderFactory {

    private final Map<String, DictionaryPageLoader> loaders;

    public DictionaryPageLoaderFactoryImpl(List<DictionaryPageLoader> dictionaryPageLoaders) {
        this.loaders = new HashMap<>();
        dictionaryPageLoaders.forEach(
                loader -> loaders.put(loader.getPageFilter().getFilter(), loader));
    }

    public DictionaryPageLoader getLoader(PageFilter filter) {
        return loaders.get(filter.getFilter());
    }
}
