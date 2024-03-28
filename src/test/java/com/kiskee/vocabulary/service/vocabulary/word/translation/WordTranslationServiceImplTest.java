package com.kiskee.vocabulary.service.vocabulary.word.translation;

import com.kiskee.vocabulary.mapper.dictionary.WordTranslationMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WordTranslationServiceImplTest {

    @InjectMocks
    private WordTranslationServiceImpl wordTranslationService;

    @Mock
    private WordTranslationMapper mapper;

    @Test
    void testUpdateTranslations_WhenGivenNewTranslationsHaveNullId_ThenUpdateTranslations() {
        List<WordTranslationDto> translationsToUpdate = List.of(
                new WordTranslationDto(1L, "переклад1"),
                new WordTranslationDto(2L, "переклад2"),
                new WordTranslationDto(null, "новий переклад1"),
                new WordTranslationDto(null, "новий переклад2"));
        List<WordTranslation> existingTranslations = List.of(
                new WordTranslation(1L, "переклад1"),
                new WordTranslation(2L, "переклад2"));

        List<WordTranslation> translationToUpdateEntities = List.of(
                new WordTranslation(null, "новий переклад1"),
                new WordTranslation(null, "новий переклад2"));

        when(mapper.toEntities(translationsToUpdate)).thenReturn(translationToUpdateEntities);

        List<WordTranslation> expectedUpdatedTranslations = List.of(
                new WordTranslation(1L, "переклад1"),
                new WordTranslation(2L, "переклад2"),
                new WordTranslation(null, "новий переклад1"),
                new WordTranslation(null, "новий переклад2"));

        List<WordTranslation> updatedTranslations = wordTranslationService.updateTranslations(
                translationsToUpdate, existingTranslations);

        assertThat(updatedTranslations)
                .containsExactlyInAnyOrderElementsOf(expectedUpdatedTranslations);
    }

    @Test
    void testUpdateTranslations_WhenNewTranslationsHaveIdThatDoesNotExistInExistingTranslations_ThenUpdateTranslationsWithoutNotExistingId() {
        List<WordTranslationDto> translationsToUpdate = List.of(
                new WordTranslationDto(1L, "переклад1"),
                new WordTranslationDto(2L, "переклад2"),
                new WordTranslationDto(null, "новий переклад1"),
                new WordTranslationDto(null, "новий переклад2"),
                new WordTranslationDto(3L, "переклад з неіснуючим id"));
        List<WordTranslation> existingTranslations = List.of(
                new WordTranslation(1L, "переклад1"),
                new WordTranslation(2L, "переклад2"));

        List<WordTranslation> translationToUpdateEntities = List.of(
                new WordTranslation(null, "новий переклад1"),
                new WordTranslation(null, "новий переклад2"));

        when(mapper.toEntities(translationsToUpdate)).thenReturn(translationToUpdateEntities);

        List<WordTranslation> expectedUpdatedTranslations = List.of(
                new WordTranslation(1L, "переклад1"),
                new WordTranslation(2L, "переклад2"),
                new WordTranslation(null, "новий переклад1"),
                new WordTranslation(null, "новий переклад2"));

        List<WordTranslation> updatedTranslations = wordTranslationService.updateTranslations(
                translationsToUpdate, existingTranslations);

        assertThat(updatedTranslations)
                .containsExactlyInAnyOrderElementsOf(expectedUpdatedTranslations);
    }

}
