package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader;

import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;

public interface RepetitionWordLoaderFactory {

    RepetitionWordCriteriaLoader getLoader(DefaultCriteriaFilter.CriteriaFilterType criteriaFilterType);
}
