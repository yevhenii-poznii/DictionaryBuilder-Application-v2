package com.kiskee.vocabulary.service.vocabulary.word.page;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.vocabulary.WordRepository;
import com.kiskee.vocabulary.util.IdentityUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Slf4j
@Getter
@AllArgsConstructor
public abstract class AbstractDictionaryPageLoader {

    private final WordRepository repository;
    private final DictionaryPageMapper mapper;

    private static final String ORDER_BY = "addedAt";

    public abstract PageFilter getPageFilter();

    protected abstract List<Word> loadWordsByFilter(List<Long> wordIds);

    protected abstract Sort.Direction getSortDirection();

    public DictionaryPageResponseDto loadDictionaryPage(Long dictionaryId, PageRequest pageRequest) {
        return loadPage(dictionaryId, pageRequest);
    }

    protected Page<WordIdDto> findWordIdsPage(Long dictionaryId, Pageable pageable) {
        return repository.findByDictionaryId(dictionaryId, pageable);
    }

    protected Pageable pageableWithSort(PageRequest pageRequest) {
        Sort.Direction sort = getSortDirection();

        return pageRequest.withSort(sort, ORDER_BY);
    }

    private DictionaryPageResponseDto loadPage(Long dictionaryId, PageRequest pageRequest) {
        Pageable pageableWithSort = pageableWithSort(pageRequest);

        Page<WordIdDto> page = findWordIdsPage(dictionaryId, pageableWithSort);

        List<Long> wordIds = page.stream()
                .map(WordIdDto::getId)
                .toList();

        List<Word> words = loadWordsByFilter(wordIds);

        log.info("Loaded [{}] words from [{}] dictionary for [{}] by [{}] filter", words.size(), dictionaryId,
                IdentityUtil.getUserId(), getPageFilter());

        return mapper.toDto(words, page.getTotalPages(), page.getTotalElements());
    }

}
