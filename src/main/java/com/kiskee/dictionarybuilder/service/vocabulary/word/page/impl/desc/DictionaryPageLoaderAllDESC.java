package com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.desc;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.vocabulary.DictionaryPageRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.AbstractDictionaryPageLoaderDESC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DictionaryPageLoaderAllDESC extends AbstractDictionaryPageLoaderDESC implements DictionaryPageLoader {

    @Override
    protected List<Word> loadWordsByFilter(List<Long> wordIds) {
        return getRepository().findByIdInOrderByAddedAtDesc(wordIds);
    }

    @Override
    public PageFilter getFilter() {
        return PageFilter.BY_ADDED_AT_DESC;
    }

    public DictionaryPageLoaderAllDESC(DictionaryPageRepository repository, DictionaryPageMapper mapper) {
        super(repository, mapper);
    }
}
