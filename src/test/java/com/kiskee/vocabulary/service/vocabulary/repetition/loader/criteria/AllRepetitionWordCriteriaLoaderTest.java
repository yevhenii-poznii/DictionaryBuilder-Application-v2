package com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria;

import com.kiskee.vocabulary.mapper.repetition.RepetitionWordMapper;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.repetition.RepetitionWordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void testLoadRepetitionWordPage_WhenGivenREPETITION_ONLYRepetitionFilter_ThenLoadWordsByFilters() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest request = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        Word word = mock(Word.class);
        when(word.getDictionaryId()).thenReturn(dictionaryId);

        when(repository.findByDictionaryIdAndUseInRepetition(dictionaryId, true))
                .thenReturn(List.of(word));

        when(mapper.toDto(wordsCaptor.capture())).thenReturn(List.of(mock(WordDto.class)));

        allRepetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, request);

        List<Word> words = wordsCaptor.getValue();
        assertThat(words.getFirst().getDictionaryId()).isEqualTo(dictionaryId);
    }
}
