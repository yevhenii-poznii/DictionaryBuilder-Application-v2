package com.kiskee.vocabulary.service.vocabulary.word.page.asc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.vocabulary.DictionaryPageRepository;
import com.kiskee.vocabulary.service.vocabulary.word.page.impl.asc.DictionaryPageLoaderOnlyUseInRepetitionASC;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class DictionaryPageLoaderOnlyUseInRepetitionASCTest {

    @InjectMocks
    private DictionaryPageLoaderOnlyUseInRepetitionASC dictionaryPageLoaderOnlyUseInRepetitionASC;

    @Mock
    private DictionaryPageRepository repository;

    @Mock
    private DictionaryPageMapper mapper;

    @Test
    void
            testGetPageFilter_WhenDictionaryPageLoaderOnlyUseInRepetitionASC_ThenReturnPageFilterONLY_USE_IN_REPETITION_ASC() {
        PageFilter pageFilter = dictionaryPageLoaderOnlyUseInRepetitionASC.getPageFilter();

        assertThat(pageFilter).isEqualTo(PageFilter.ONLY_USE_IN_REPETITION_ASC);
    }

    @Test
    void testLoadDictionaryPage_WhenGivenValidDictionaryIdAndPageRequest_ThenReturnDictionaryPageResponseDto() {
        Long dictionaryId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 100);

        Page page = mock(Page.class);
        List<WordIdDto> wordIdDtos = List.of(new WordIdDto(1L), new WordIdDto(2L));
        when(page.stream()).thenReturn(wordIdDtos.stream());
        when(page.getTotalPages()).thenReturn(1);
        when(page.getTotalElements()).thenReturn(2L);

        when(repository.findByDictionaryIdAndUseInRepetition(eq(dictionaryId), eq(true), any(PageRequest.class)))
                .thenReturn(page);

        List<Word> words = List.of(
                new Word(1L, "word1", true, 0, null, null, null, 1L, List.of()),
                new Word(2L, "word2", true, 0, null, null, null, 1L, List.of()));
        when(repository.findByIdInAndUseInRepetitionOrderByAddedAtAsc(List.of(1L, 2L), true))
                .thenReturn(words);

        List<WordDto> wordDtos = List.of(
                new WordDto(1L, "word1", true, List.of(), "hint1"), new WordDto(2L, "word2", true, List.of(), "hint2"));
        DictionaryPageResponseDto expectedResult =
                new DictionaryPageResponseDto(wordDtos, page.getTotalPages(), (int) page.getTotalElements());
        when(mapper.toDto(words, page.getTotalPages(), page.getTotalElements())).thenReturn(expectedResult);

        DictionaryPageResponseDto result =
                dictionaryPageLoaderOnlyUseInRepetitionASC.loadDictionaryPage(dictionaryId, pageRequest);

        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getWords()).extracting(WordDto::getWord).containsExactlyInAnyOrder("word1", "word2");
    }
}
