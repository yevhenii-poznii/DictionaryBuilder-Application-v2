package com.kiskee.dictionarybuilder.service.vocabulary.word.page;

import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import org.springframework.data.domain.PageRequest;

public interface DictionaryPageLoader {

    PageFilter getPageFilter();

    DictionaryPageResponseDto loadDictionaryPage(Long dictionaryId, PageRequest pageRequest);
}
