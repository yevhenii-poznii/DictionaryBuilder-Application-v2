package com.kiskee.dictionarybuilder.model.entity.redis.repetition;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import java.util.List;

public interface RepetitionDataDto {

    WordDto getCurrentWord();

    String getWord();

    int getRightAnswersCount();

    int getWrongAnswersCount();

    int getSkippedWordsCount();

    int getTotalElements();

    int getTotalElementsPassed();

    boolean isReversed();

    default List<String> getTranslationOptions() {
        return null;
    }

    default String getWordHint() {
        return getCurrentWord().getWordHint();
    }
}
