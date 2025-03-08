package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler;

import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionProgressUpdater;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractRepetitionHandler {

    private final RepetitionDataRepository repository;
    private final RepetitionProgressUpdater repetitionProgressUpdater;

    protected RepetitionData getRepetitionData(UUID userId) {
        return repository
                .findById(userId.toString())
                .orElseThrow(() -> new RepetitionException("Repetition is not running"));
    }
}
