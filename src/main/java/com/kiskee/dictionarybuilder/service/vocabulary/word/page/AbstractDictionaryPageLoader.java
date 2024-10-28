package com.kiskee.dictionarybuilder.service.vocabulary.word.page;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.vocabulary.DictionaryPageRepository;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Slf4j
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDictionaryPageLoader {

    private final DictionaryPageRepository repository;
    private final DictionaryPageMapper mapper;

    private static final String ORDER_BY = "addedAt";

    public abstract PageFilter getFilter();

    protected abstract List<Word> loadWordsByFilter(List<Long> wordIds);

    protected abstract Sort.Direction getSortDirection();

    public DictionaryPageResponseDto loadWords(Long dictionaryId, PageRequest pageRequest) {
        return loadPage(dictionaryId, pageRequest);
    }

    protected Page<Long> findWordIdsPage(Long dictionaryId, Pageable pageable) {
        return repository.findByDictionaryId(dictionaryId, pageable);
    }

    protected Pageable pageableWithSort(PageRequest pageRequest) {
        Sort.Direction sort = getSortDirection();
        return pageRequest.withSort(sort, ORDER_BY);
    }

    private DictionaryPageResponseDto loadPage(Long dictionaryId, PageRequest pageRequest) {
        Pageable pageableWithSort = pageableWithSort(pageRequest);
        Page<Long> page = findWordIdsPage(dictionaryId, pageableWithSort);
        List<Word> words = loadWordsByFilter(page.toList());

        log.info(
                "Loaded [{}] words from [{}] dictionary for [{}] by [{}] filter",
                words.size(),
                dictionaryId,
                IdentityUtil.getUserId(),
                getFilter());

        return mapper.toDto(words, page.getTotalPages(), page.getTotalElements());
    }
}
