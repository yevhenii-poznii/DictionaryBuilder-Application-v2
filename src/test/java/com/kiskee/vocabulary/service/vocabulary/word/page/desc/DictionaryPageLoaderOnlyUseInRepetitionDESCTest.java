package com.kiskee.vocabulary.service.vocabulary.word.page.desc;

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
import com.kiskee.vocabulary.service.vocabulary.word.page.impl.desc.DictionaryPageLoaderOnlyUseInRepetitionDESC;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class DictionaryPageLoaderOnlyUseInRepetitionDESCTest {

    @InjectMocks
    private DictionaryPageLoaderOnlyUseInRepetitionDESC dictionaryPageLoaderOnlyUseInRepetitionDESC;

    @Mock
    private DictionaryPageRepository repository;

    @Mock
    private DictionaryPageMapper mapper;

    @Test
    void
            testGetPageFilter_WhenDictionaryPageLoaderOnlyUseInRepetitionDESC_ThenReturnPageFilterONLY_USE_IN_REPETITION_DESC() {
        PageFilter pageFilter = dictionaryPageLoaderOnlyUseInRepetitionDESC.getPageFilter();

        assertThat(pageFilter).isEqualTo(PageFilter.ONLY_USE_IN_REPETITION_DESC);
    }

    @Test
    void testLoadDictionaryPage_WhenGivenValidDictionaryIdAndPageRequest_ThenReturnDictionaryPageResponseDto() {
        Long dictionaryId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 100);

        Page page = mock(Page.class);
        List<WordIdDto> wordIdDtos = List.of(new WordIdDto(2L), new WordIdDto(1L));
        when(page.stream()).thenReturn(wordIdDtos.stream());
        when(page.getTotalPages()).thenReturn(1);
        when(page.getTotalElements()).thenReturn(2L);

        when(repository.findByDictionaryIdAndUseInRepetition(eq(dictionaryId), eq(true), any(PageRequest.class)))
                .thenReturn(page);

        List<Word> words = List.of(
                new Word(2L, "word2", true, 0, null, null, null, 1L, List.of()),
                new Word(1L, "word1", true, 0, null, null, null, 1L, List.of()));
        when(repository.findByIdInAndUseInRepetitionOrderByAddedAtDesc(List.of(2L, 1L), true))
                .thenReturn(words);

        List<WordDto> wordDtos = List.of(
                new WordDto(2L, "word2", true, List.of(), null), new WordDto(1L, "word1", true, List.of(), null));
        DictionaryPageResponseDto expectedResult =
                new DictionaryPageResponseDto(wordDtos, page.getTotalPages(), (int) page.getTotalElements());
        when(mapper.toDto(words, page.getTotalPages(), page.getTotalElements())).thenReturn(expectedResult);

        DictionaryPageResponseDto result =
                dictionaryPageLoaderOnlyUseInRepetitionDESC.loadDictionaryPage(dictionaryId, pageRequest);

        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getWords()).extracting(WordDto::getWord).containsExactlyInAnyOrder("word1", "word2");
    }
}
