package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import java.util.List;

public interface RepetitionWordCriteriaLoader {

    DefaultCriteriaFilter.CriteriaFilterType getCriteriaFilter();

    List<WordDto> loadRepetitionWordPage(Long dictionaryId, RepetitionStartFilterRequest request);
}
