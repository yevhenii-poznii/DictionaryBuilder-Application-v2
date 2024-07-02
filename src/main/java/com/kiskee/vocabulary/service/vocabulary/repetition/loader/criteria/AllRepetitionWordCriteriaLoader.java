package com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria;

import com.kiskee.vocabulary.mapper.repetition.RepetitionWordMapper;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.repetition.RepetitionWordRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AllRepetitionWordCriteriaLoader extends AbstractRepetitionWordCriteriaLoader
        implements RepetitionWordCriteriaLoader {

    public AllRepetitionWordCriteriaLoader(RepetitionWordRepository repository, RepetitionWordMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public DefaultCriteriaFilter.CriteriaFilterType getCriteriaFilter() {
        return DefaultCriteriaFilter.CriteriaFilterType.ALL;
    }

    @Override
    protected List<Word> loadRepetitionOnly(Long dictionaryId, RepetitionStartFilterRequest request) {
        return getRepository().findByDictionaryIdAndUseInRepetition(dictionaryId, true);
    }

    @Override
    protected List<Word> loadNotRepetitionOnly(Long dictionaryId, RepetitionStartFilterRequest request) {
        return getRepository().findByDictionaryIdAndUseInRepetition(dictionaryId, false);
    }

    @Override
    protected List<Word> loadAll(Long dictionaryId, RepetitionStartFilterRequest request) {
        return getRepository().findByDictionaryId(dictionaryId);
    }
}
