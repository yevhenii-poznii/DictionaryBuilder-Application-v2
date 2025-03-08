package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.criteria.CountCriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartFilterRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CountRepetitionWordCriteriaLoaderTest {

    @InjectMocks
    private CountRepetitionWordCriteriaLoader countRepetitionWordCriteriaLoader;

    @Mock
    private RepetitionWordRepository repository;

    @Mock
    private RepetitionWordMapper mapper;

    @Captor
    private ArgumentCaptor<List<Word>> wordsCaptor;

    @Test
    void testGetCriteriaFilter_WhenInvoked_ThenReturnCriteriaFilterType() {
        CriteriaFilterType criteriaFilter = countRepetitionWordCriteriaLoader.getFilter();

        assertThat(criteriaFilter).isEqualTo(CriteriaFilterType.BY_COUNT);
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenRepetitionOnlyRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new CountCriteriaFilter(10),
                RepetitionType.INPUT,
                false);

        Page<Long> wordIds = new PageImpl<>(List.of(1L));
        when(repository.findByDictionaryIdAndUseInRepetition(eq(dictionaryId), eq(true), any(Pageable.class)))
                .thenReturn(wordIds);

        Word word = mock(Word.class);
        when(word.getDictionaryId()).thenReturn(dictionaryId);
        when(word.isUseInRepetition()).thenReturn(true);

        when(repository.findByIdIn(List.of(1L))).thenReturn(List.of(word));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class)));

        countRepetitionWordCriteriaLoader.loadWords(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words.getFirst().getDictionaryId()).isEqualTo(dictionaryId);
        assertThat(words.getFirst().isUseInRepetition()).isTrue();
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenNotRepetitionOnlyRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.NOT_REPETITION_ONLY,
                new CountCriteriaFilter(10),
                RepetitionType.INPUT,
                false);

        Page<Long> wordIds = new PageImpl<>(List.of(1L));
        when(repository.findByDictionaryIdAndUseInRepetition(eq(dictionaryId), eq(false), any(Pageable.class)))
                .thenReturn(wordIds);

        Word word = mock(Word.class);
        when(word.getDictionaryId()).thenReturn(dictionaryId);
        when(word.isUseInRepetition()).thenReturn(false);

        when(repository.findByIdIn(List.of(1L))).thenReturn(List.of(word));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class)));

        countRepetitionWordCriteriaLoader.loadWords(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words.getFirst().getDictionaryId()).isEqualTo(dictionaryId);
        assertThat(words.getFirst().isUseInRepetition()).isFalse();
    }

    @Test
    void testLoadRepetitionWordPage_WhenGivenAllRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.ALL,
                new CountCriteriaFilter(10),
                RepetitionType.INPUT,
                false);

        Page<Long> wordIds = new PageImpl<>(List.of(1L, 2L));
        when(repository.findByDictionaryId(eq(dictionaryId), any(Pageable.class)))
                .thenReturn(wordIds);

        Word firstWord = mock(Word.class);
        when(firstWord.getDictionaryId()).thenReturn(dictionaryId);
        when(firstWord.isUseInRepetition()).thenReturn(true);
        Word secondWord = mock(Word.class);
        when(secondWord.getDictionaryId()).thenReturn(dictionaryId);
        when(secondWord.isUseInRepetition()).thenReturn(false);

        when(repository.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(firstWord, secondWord));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class), mock(WordDto.class)));

        countRepetitionWordCriteriaLoader.loadWords(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words)
                .extracting(Word::getDictionaryId)
                .containsExactlyElementsOf(List.of(dictionaryId, dictionaryId));
        assertThat(words).extracting(Word::isUseInRepetition).containsExactlyElementsOf(List.of(true, false));
    }
}
