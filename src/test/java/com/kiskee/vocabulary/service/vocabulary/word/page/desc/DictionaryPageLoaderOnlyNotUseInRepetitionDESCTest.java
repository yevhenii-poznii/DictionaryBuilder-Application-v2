package com.kiskee.vocabulary.service.vocabulary.word.page.desc;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordHintDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.model.entity.vocabulary.WordHint;
import com.kiskee.vocabulary.repository.vocabulary.WordRepository;
import com.kiskee.vocabulary.service.vocabulary.word.page.impl.desc.DictionaryPageLoaderOnlyNotUseInRepetitionDESC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DictionaryPageLoaderOnlyNotUseInRepetitionDESCTest {

    @InjectMocks
    private DictionaryPageLoaderOnlyNotUseInRepetitionDESC dictionaryPageLoaderOnlyNotUseInRepetitionDESC;

    @Mock
    private WordRepository repository;
    @Mock
    private DictionaryPageMapper mapper;

    @Test
    void testGetPageFilter_WhenDictionaryPageLoaderOnlyNotUseInRepetitionDESC_ThenReturnPageFilterONLY_NOT_USE_IN_REPETITION_DESC() {
        PageFilter pageFilter = dictionaryPageLoaderOnlyNotUseInRepetitionDESC.getPageFilter();

        assertThat(pageFilter).isEqualTo(PageFilter.ONLY_NOT_USE_IN_REPETITION_DESC);
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

        when(repository.findByDictionaryIdAndUseInRepetition(eq(dictionaryId), eq(false), any(PageRequest.class))).thenReturn(page);

        List<Word> words = List.of(
                new Word(2L, "word2", false, 0, null, null, 1L, List.of(), mock(WordHint.class)),
                new Word(1L, "word1", false, 0, null, null, 1L, List.of(), mock(WordHint.class))
        );
        when(repository.findByIdInAndUseInRepetitionOrderByAddedAtDesc(List.of(2L, 1L), false)).thenReturn(words);

        List<WordDto> wordDtos = List.of(
                new WordDto(2L, "word2", false, List.of(), mock(WordHintDto.class)),
                new WordDto(1L, "word1", false, List.of(), mock(WordHintDto.class))
        );
        DictionaryPageResponseDto expectedResult = new DictionaryPageResponseDto(wordDtos,
                page.getTotalPages(), (int) page.getTotalElements());
        when(mapper.toDto(words, page.getTotalPages(), page.getTotalElements())).thenReturn(expectedResult);

        DictionaryPageResponseDto result = dictionaryPageLoaderOnlyNotUseInRepetitionDESC.loadDictionaryPage(dictionaryId, pageRequest);

        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getWords()).extracting(WordDto::getWord)
                .containsExactlyInAnyOrder("word1", "word2");
    }

}
