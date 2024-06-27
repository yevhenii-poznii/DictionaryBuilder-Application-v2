package com.kiskee.vocabulary.service.vocabulary.word.page;

import com.kiskee.vocabulary.mapper.repetition.RepetitionWordMapper;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.repetition.RepetitionWordRepository;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRepetitionWordCriteriaLoader {

    private final RepetitionWordRepository repository;
    private final RepetitionWordMapper mapper;

    protected abstract List<Word> loadRepetitionOnly(Long dictionaryId, RepetitionStartFilterRequest request);

    protected abstract List<Word> loadNotRepetitionOnly(Long dictionaryId, RepetitionStartFilterRequest request);

    protected abstract List<Word> loadAll(Long dictionaryId, RepetitionStartFilterRequest request);

    public List<WordDto> loadRepetitionWordPage(Long dictionaryId, RepetitionStartFilterRequest request) {
        List<Word> repetitionWords = load(dictionaryId, request);
        return mapper.toDto(repetitionWords);
    }

    private List<Word> load(Long dictionaryId, RepetitionStartFilterRequest request) {
        return switch (request.getRepetitionFilter()) {
            case REPETITION_ONLY -> loadRepetitionOnly(dictionaryId, request);
            case NOT_REPETITION_ONLY -> loadNotRepetitionOnly(dictionaryId, request);
            case ALL -> loadAll(dictionaryId, request);
        };
    }
}
