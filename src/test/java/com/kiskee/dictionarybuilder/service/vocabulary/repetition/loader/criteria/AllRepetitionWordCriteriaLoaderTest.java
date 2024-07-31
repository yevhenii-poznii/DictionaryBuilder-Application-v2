package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.repetition.RepetitionWordRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AllRepetitionWordCriteriaLoaderTest {

    @InjectMocks
    private AllRepetitionWordCriteriaLoader allRepetitionWordCriteriaLoader;

    @Mock
    private RepetitionWordRepository repository;

    @Mock
    private RepetitionWordMapper mapper;

    @Captor
    private ArgumentCaptor<List<Word>> wordsCaptor;

    @Test
    void testGetCriteriaFilter_WhenInvoked_ThenReturnCriteriaFilterType() {
        DefaultCriteriaFilter.CriteriaFilterType criteriaFilter = allRepetitionWordCriteriaLoader.getCriteriaFilter();

        assertThat(criteriaFilter).isEqualTo(DefaultCriteriaFilter.CriteriaFilterType.ALL);
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenRepetitionOnlyRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        Word word = mock(Word.class);
        when(word.getDictionaryId()).thenReturn(dictionaryId);
        when(word.isUseInRepetition()).thenReturn(true);

        when(repository.findByDictionaryIdAndUseInRepetition(dictionaryId, true))
                .thenReturn(List.of(word));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class)));

        allRepetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words.getFirst().getDictionaryId()).isEqualTo(dictionaryId);
        assertThat(words.getFirst().isUseInRepetition()).isTrue();
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenNotRepetitionOnlyRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.NOT_REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        Word word = mock(Word.class);
        when(word.getDictionaryId()).thenReturn(dictionaryId);
        when(word.isUseInRepetition()).thenReturn(false);

        when(repository.findByDictionaryIdAndUseInRepetition(dictionaryId, false))
                .thenReturn(List.of(word));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class)));

        allRepetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words.getFirst().getDictionaryId()).isEqualTo(dictionaryId);
        assertThat(words.getFirst().isUseInRepetition()).isFalse();
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenAllRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.ALL,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        Word firstWord = mock(Word.class);
        when(firstWord.getDictionaryId()).thenReturn(dictionaryId);
        when(firstWord.isUseInRepetition()).thenReturn(true);
        Word secondWord = mock(Word.class);
        when(secondWord.getDictionaryId()).thenReturn(dictionaryId);
        when(secondWord.isUseInRepetition()).thenReturn(false);

        when(repository.findByDictionaryId(dictionaryId)).thenReturn(List.of(firstWord, secondWord));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class), mock(WordDto.class)));

        allRepetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words)
                .extracting(Word::getDictionaryId)
                .containsExactlyElementsOf(List.of(dictionaryId, dictionaryId));
        assertThat(words).extracting(Word::isUseInRepetition).containsExactlyElementsOf(List.of(true, false));
    }
}
