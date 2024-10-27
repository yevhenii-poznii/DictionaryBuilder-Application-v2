package com.kiskee.dictionarybuilder.service.vocabulary.loader.factory;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.WordFilter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class WordLoaderFactoryImpl<F extends Enum<F> & WordFilter, L extends WordLoader<F, ?, ?>>
        implements WordLoaderFactory<F, L> {

    private final Map<String, L> loaders;

    public WordLoaderFactoryImpl(List<L> dictionaryPageLoaders) {
        System.out.println(dictionaryPageLoaders);
        this.loaders = dictionaryPageLoaders.stream()
                .collect(Collectors.toMap(loader -> loader.getFilter().name(), Function.identity()));
    }

    @Override
    public L getLoader(F filter) {
        return loaders.get(filter.name());
    }
}
