package com.kiskee.vocabulary.service.vocabulary.repetition.loader;

import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;

public interface RepetitionWordLoaderFactory {

    RepetitionWordCriteriaLoader getLoader(DefaultCriteriaFilter.CriteriaFilterType criteriaFilterType);
}
