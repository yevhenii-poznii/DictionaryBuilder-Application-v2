package com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.asc;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.vocabulary.DictionaryPageRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.AbstractDictionaryPageLoaderASC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DictionaryPageLoaderAllASC extends AbstractDictionaryPageLoaderASC implements DictionaryPageLoader {

    @Override
    protected List<Word> loadWordsByFilter(List<Long> wordIds) {
        return getRepository().findByIdInOrderByAddedAtAsc(wordIds);
    }

    @Override
    public PageFilter getFilter() {
        return PageFilter.BY_ADDED_AT_ASC;
    }

    public DictionaryPageLoaderAllASC(DictionaryPageRepository repository, DictionaryPageMapper mapper) {
        super(repository, mapper);
    }
}
