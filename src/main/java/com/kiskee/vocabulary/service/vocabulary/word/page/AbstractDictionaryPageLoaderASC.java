package com.kiskee.vocabulary.service.vocabulary.word.page;

import com.kiskee.vocabulary.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.vocabulary.repository.vocabulary.DictionaryPageRepository;
import org.springframework.data.domain.Sort;

public abstract class AbstractDictionaryPageLoaderASC extends AbstractDictionaryPageLoader {

    public AbstractDictionaryPageLoaderASC(DictionaryPageRepository repository, DictionaryPageMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected final Sort.Direction getSortDirection() {
        return Sort.Direction.ASC;
    }
}
