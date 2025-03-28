package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria;

import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.repetition.RepetitionWordRepository;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractRepetitionWordCriteriaLoader {

    private final RepetitionWordRepository repository;
    private final RepetitionWordMapper mapper;

    protected abstract List<Word> loadRepetitionOnly(Long dictionaryId, RepetitionStartRequest request);

    protected abstract List<Word> loadNotRepetitionOnly(Long dictionaryId, RepetitionStartRequest request);

    protected abstract List<Word> loadAll(Long dictionaryId, RepetitionStartRequest request);

    public List<WordDto> loadWords(Long dictionaryId, RepetitionStartRequest request) {
        List<Word> repetitionWords = loadByFilter(dictionaryId, request);
        log.info("Loaded {} words for repetition for user: {}", repetitionWords.size(), IdentityUtil.getUserId());
        return mapper.toDto(repetitionWords);
    }

    private List<Word> loadByFilter(Long dictionaryId, RepetitionStartRequest request) {
        return switch (request.getRepetitionFilter()) {
            case REPETITION_ONLY -> loadRepetitionOnly(dictionaryId, request);
            case NOT_REPETITION_ONLY -> loadNotRepetitionOnly(dictionaryId, request);
            case ALL -> loadAll(dictionaryId, request);
        };
    }
}
