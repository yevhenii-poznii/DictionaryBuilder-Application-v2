package com.kiskee.dictionarybuilder.util.repetition;

import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RepetitionUtils {

    public static void validateNextNonNull(WordDto nextWord) {
        if (Objects.isNull(nextWord)) {
            throw new RepetitionException("No more words to repeat");
        }
    }

    public static long calculateCorrectTranslationsCount(
            Set<String> wordTranslations, List<String> translationsToCheck) {
        return translationsToCheck.stream().filter(wordTranslations::contains).count();
    }
}
