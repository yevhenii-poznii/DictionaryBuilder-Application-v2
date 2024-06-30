package com.kiskee.vocabulary.service.vocabulary.word;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import java.util.List;
import java.util.UUID;

public interface WordCounterUpdateService {

    void updateRightAnswersCounters(UUID userId, List<WordDto> wordsToUpdate);
}
