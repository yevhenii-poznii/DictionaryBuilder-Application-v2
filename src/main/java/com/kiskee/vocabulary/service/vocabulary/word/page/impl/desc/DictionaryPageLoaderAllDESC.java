package com.kiskee.vocabulary.service.vocabulary.word.page.impl.desc;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.vocabulary.WordRepository;
import com.kiskee.vocabulary.service.vocabulary.word.page.AbstractDictionaryPageLoaderDESC;
import com.kiskee.vocabulary.service.vocabulary.word.page.DictionaryPageLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryPageLoaderAllDESC extends AbstractDictionaryPageLoaderDESC
        implements DictionaryPageLoader {

    @Override
    protected List<Word> loadWordsByFilter(List<Long> wordIds) {
        return getRepository().findByIdInOrderByAddedAtDesc(wordIds);
    }

    @Override
    public PageFilter getPageFilter() {
        return PageFilter.BY_ADDED_AT_DESC;
    }

    public DictionaryPageLoaderAllDESC(WordRepository repository, DictionaryPageMapper mapper) {
        super(repository, mapper);
    }

}
