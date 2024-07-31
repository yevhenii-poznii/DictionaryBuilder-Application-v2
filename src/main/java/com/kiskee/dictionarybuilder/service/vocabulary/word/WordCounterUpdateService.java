package com.kiskee.dictionarybuilder.service.vocabulary.word;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import java.util.List;
import java.util.UUID;

public interface WordCounterUpdateService {

    void updateRightAnswersCounters(UUID userId, List<WordDto> wordsToUpdate);
}
