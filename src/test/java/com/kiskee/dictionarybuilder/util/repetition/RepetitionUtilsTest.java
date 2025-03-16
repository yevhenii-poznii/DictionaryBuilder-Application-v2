package com.kiskee.dictionarybuilder.util.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RepetitionUtilsTest {

    @Test
    void testValidateNextNonNull_WhenNextWordIsNonNull_ThenDoNothing() {
        WordDto wordDto = mock(WordDto.class);
        RepetitionUtils.validateNextNonNull(wordDto);
    }

    @Test
    void testValidateNextNonNull_WhenNextWordIsNull_ThenThrowRepetitionException() {
        WordDto wordDto = null;

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> RepetitionUtils.validateNextNonNull(wordDto))
                .withMessage("No more words to repeat");
    }

    @Test
    void testCalculateCorrectTranslationsCount_WhenThereAreCorrectTranslations_ThenReturnCorrectTranslationsCount() {
        Set<String> wordTranslations = Set.of("a", "b", "c");
        List<String> translationsToCheck = List.of("a", "d", "k", "l", "m");

        long correctTranslationsCount =
                RepetitionUtils.calculateCorrectTranslationsCount(wordTranslations, translationsToCheck);

        assertThat(correctTranslationsCount).isEqualTo(1);
    }

    @Test
    void testCalculateCorrectTranslationsCount_WhenThereAreNoCorrectTranslations_ThenReturnZero() {
        Set<String> wordTranslations = Set.of("a", "b", "c");
        List<String> translationsToCheck = List.of("d", "k", "l", "m");

        long correctTranslationsCount =
                RepetitionUtils.calculateCorrectTranslationsCount(wordTranslations, translationsToCheck);

        assertThat(correctTranslationsCount).isEqualTo(0);
    }

    @Test
    void testCalculateCorrectTranslationsCount_WhenGivenEmptyCorrectTranslationsToCheckList_ThenReturnZero() {
        Set<String> wordTranslations = Set.of("a", "b", "c");
        List<String> translationsToCheck = Collections.emptyList();

        long correctTranslationsCount =
                RepetitionUtils.calculateCorrectTranslationsCount(wordTranslations, translationsToCheck);

        assertThat(correctTranslationsCount).isEqualTo(0);
    }
}
