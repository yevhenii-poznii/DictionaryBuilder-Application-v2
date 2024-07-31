package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.criteria.DateCriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.repetition.RepetitionWordRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateRepetitionWordCriteriaLoaderTest {

    @InjectMocks
    private DateRepetitionWordCriteriaLoader dateRepetitionWordCriteriaLoader;

    @Mock
    private RepetitionWordRepository repository;

    @Mock
    private RepetitionWordMapper mapper;

    @Captor
    private ArgumentCaptor<List<Word>> wordsCaptor;

    @Test
    void testGetCriteriaFilter_WhenInvoked_ThenReturnCriteriaFilterType() {
        DefaultCriteriaFilter.CriteriaFilterType criteriaFilter = dateRepetitionWordCriteriaLoader.getCriteriaFilter();

        assertThat(criteriaFilter).isEqualTo(DefaultCriteriaFilter.CriteriaFilterType.BY_DATE);
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenRepetitionOnlyRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        LocalDate from = LocalDate.of(2024, 7, 1);
        LocalDate to = LocalDate.of(2024, 7, 4);
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DateCriteriaFilter(new DateCriteriaFilter.DateRange(from, to)));

        Word word = mock(Word.class);
        when(word.getDictionaryId()).thenReturn(dictionaryId);
        when(word.isUseInRepetition()).thenReturn(true);

        when(repository.findByDictionaryIdAndUseInRepetitionAndAddedAtBetween(
                        eq(dictionaryId), eq(true), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(word));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class)));

        dateRepetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words.getFirst().getDictionaryId()).isEqualTo(dictionaryId);
        assertThat(words.getFirst().isUseInRepetition()).isTrue();
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenNotRepetitionOnlyRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        LocalDate from = LocalDate.of(2024, 7, 1);
        LocalDate to = LocalDate.of(2024, 7, 4);
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.NOT_REPETITION_ONLY,
                new DateCriteriaFilter(new DateCriteriaFilter.DateRange(from, to)));

        Word word = mock(Word.class);
        when(word.getDictionaryId()).thenReturn(dictionaryId);
        when(word.isUseInRepetition()).thenReturn(false);

        when(repository.findByDictionaryIdAndUseInRepetitionAndAddedAtBetween(
                        eq(dictionaryId), eq(false), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(word));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class)));

        dateRepetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words.getFirst().getDictionaryId()).isEqualTo(dictionaryId);
        assertThat(words.getFirst().isUseInRepetition()).isFalse();
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenAllRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        LocalDate from = LocalDate.of(2024, 7, 1);
        LocalDate to = LocalDate.of(2024, 7, 4);
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.ALL,
                new DateCriteriaFilter(new DateCriteriaFilter.DateRange(from, to)));

        Word firstWord = mock(Word.class);
        when(firstWord.getDictionaryId()).thenReturn(dictionaryId);
        when(firstWord.isUseInRepetition()).thenReturn(true);
        Word secondWord = mock(Word.class);
        when(secondWord.getDictionaryId()).thenReturn(dictionaryId);
        when(secondWord.isUseInRepetition()).thenReturn(false);

        when(repository.findByDictionaryIdAndAddedAtBetween(eq(dictionaryId), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(firstWord, secondWord));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class)));

        dateRepetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words)
                .extracting(Word::getDictionaryId)
                .containsExactlyElementsOf(List.of(dictionaryId, dictionaryId));
        assertThat(words).extracting(Word::isUseInRepetition).containsExactlyElementsOf(List.of(true, false));
    }
}
