package com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria;

import com.kiskee.vocabulary.mapper.repetition.RepetitionWordMapper;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.repetition.RepetitionWordRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CountRepetitionWordCriteriaLoader extends AbstractRepetitionWordCriteriaLoader
        implements RepetitionWordCriteriaLoader {

    public CountRepetitionWordCriteriaLoader(RepetitionWordRepository repository, RepetitionWordMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public DefaultCriteriaFilter.CriteriaFilterType getCriteriaFilter() {
        return DefaultCriteriaFilter.CriteriaFilterType.BY_COUNT;
    }

    @Override
    protected List<Word> loadRepetitionOnly(Long dictionaryId, RepetitionStartFilterRequest request) {
        List<Long> wordIds = loadIdsByUseInRepetition(dictionaryId, true, request);
        return getRepository().findByIdIn(wordIds);
    }

    @Override
    protected List<Word> loadNotRepetitionOnly(Long dictionaryId, RepetitionStartFilterRequest request) {
        List<Long> wordIds = loadIdsByUseInRepetition(dictionaryId, false, request);
        return getRepository().findByIdIn(wordIds);
    }

    @Override
    protected List<Word> loadAll(Long dictionaryId, RepetitionStartFilterRequest request) {
        Pageable pageable = buildPageable(request);

        List<Long> wordIds = getRepository().findByDictionaryId(dictionaryId, pageable).stream()
                .map(WordIdDto::getId)
                .toList();

        return getRepository().findByIdIn(wordIds);
    }

    private Pageable buildPageable(RepetitionStartFilterRequest request) {
        return PageRequest.of(
                0,
                (Integer) request.getCriteriaFilter().getFilterValue(),
                Sort.by("addedAt").descending());
    }

    private List<Long> loadIdsByUseInRepetition(
            Long dictionaryId, boolean useInRepetition, RepetitionStartFilterRequest request) {
        Pageable pageable = buildPageable(request);
        return getRepository().findByDictionaryIdAndUseInRepetition(dictionaryId, useInRepetition, pageable).stream()
                .map(WordIdDto::getId)
                .toList();
    }
}
