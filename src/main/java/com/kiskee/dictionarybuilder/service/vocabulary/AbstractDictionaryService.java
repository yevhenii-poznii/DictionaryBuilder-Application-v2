package com.kiskee.dictionarybuilder.service.vocabulary;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.service.vocabulary.loader.factory.WordLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;

public abstract class AbstractDictionaryService {

    protected abstract WordLoaderFactory<PageFilter, DictionaryPageLoader> getDictionaryPageLoaderFactory();

    protected DictionaryPageResponseDto load(long dictionaryId, DictionaryPageRequestDto request) {
        int page = Optional.ofNullable(request.getPage()).orElse(0);
        int size = Optional.ofNullable(request.getSize()).orElse(100);
        PageFilter pageFilter = Optional.ofNullable(request.getFilter()).orElse(PageFilter.BY_ADDED_AT_ASC);
        PageRequest pageRequest = PageRequest.of(page, size);
        DictionaryPageLoader dictionaryPageLoader =
                getDictionaryPageLoaderFactory().getLoader(pageFilter);
        return dictionaryPageLoader.loadWords(dictionaryId, pageRequest);
    }
}
