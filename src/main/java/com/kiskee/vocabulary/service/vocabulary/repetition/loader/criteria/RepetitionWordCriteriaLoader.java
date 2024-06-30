package com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria;

import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import java.util.List;

public interface RepetitionWordCriteriaLoader {

    DefaultCriteriaFilter.CriteriaFilterType getCriteriaFilter();

    List<WordDto> loadRepetitionWordPage(Long dictionaryId, RepetitionStartFilterRequest request);
}
