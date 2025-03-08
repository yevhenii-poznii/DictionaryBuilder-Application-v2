package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.service.vocabulary.loader.factory.WordLoader;
import java.util.List;

public interface RepetitionWordCriteriaLoader
        extends WordLoader<CriteriaFilterType, RepetitionStartRequest, List<WordDto>> {}
