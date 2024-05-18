package com.kiskee.vocabulary.service.vocabulary.word.page;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import org.springframework.data.domain.PageRequest;

public interface DictionaryPageLoader {

    PageFilter getPageFilter();

    DictionaryPageResponseDto loadDictionaryPage(Long dictionaryId, PageRequest pageRequest);
}
