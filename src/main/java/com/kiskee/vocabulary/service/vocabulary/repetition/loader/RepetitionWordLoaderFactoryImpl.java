package com.kiskee.vocabulary.service.vocabulary.repetition.loader;

import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RepetitionWordLoaderFactoryImpl implements RepetitionWordLoaderFactory {

    private final Map<String, RepetitionWordCriteriaLoader> loaders;

    public RepetitionWordLoaderFactoryImpl(List<RepetitionWordCriteriaLoader> repetitionWordCriteriaLoaders) {
        this.loaders = new HashMap<>();
        repetitionWordCriteriaLoaders.forEach(
                loader -> loaders.put(loader.getCriteriaFilter().name(), loader));
    }

    @Override
    public RepetitionWordCriteriaLoader getLoader(DefaultCriteriaFilter.CriteriaFilterType criteriaFilterType) {
        return loaders.get(criteriaFilterType.name());
    }
}
