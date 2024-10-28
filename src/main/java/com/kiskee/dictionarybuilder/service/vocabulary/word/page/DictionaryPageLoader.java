package com.kiskee.dictionarybuilder.service.vocabulary.word.page;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.service.vocabulary.loader.factory.WordLoader;
import org.springframework.data.domain.PageRequest;

public interface DictionaryPageLoader extends WordLoader<PageFilter, PageRequest, DictionaryPageResponseDto> {}
