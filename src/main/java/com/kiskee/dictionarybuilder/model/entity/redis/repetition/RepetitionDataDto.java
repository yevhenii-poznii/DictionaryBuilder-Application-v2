package com.kiskee.dictionarybuilder.model.entity.redis.repetition;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import java.util.List;

public interface RepetitionDataDto {

    WordDto getCurrentWord();

    int getRightAnswersCount();

    int getWrongAnswersCount();

    int getSkippedWordsCount();

    int getTotalElements();

    int getTotalElementsPassed();

    default List<String> getTranslationOptions() {
        return null;
    }
}
