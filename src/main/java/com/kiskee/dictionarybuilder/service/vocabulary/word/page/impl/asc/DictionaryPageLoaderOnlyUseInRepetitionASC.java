package com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.asc;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.vocabulary.DictionaryPageRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.AbstractDictionaryPageLoaderASC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DictionaryPageLoaderOnlyUseInRepetitionASC extends AbstractDictionaryPageLoaderASC
        implements DictionaryPageLoader {

    @Override
    protected Page<Long> findWordIdsPage(Long dictionaryId, Pageable pageable) {
        return getRepository().findByDictionaryIdAndUseInRepetition(dictionaryId, true, pageable);
    }

    @Override
    protected List<Word> loadWordsByFilter(List<Long> wordIds) {
        return getRepository().findByIdInAndUseInRepetitionOrderByAddedAtAsc(wordIds, true);
    }

    @Override
    public PageFilter getFilter() {
        return PageFilter.ONLY_USE_IN_REPETITION_ASC;
    }

    public DictionaryPageLoaderOnlyUseInRepetitionASC(
            DictionaryPageRepository repository, DictionaryPageMapper mapper) {
        super(repository, mapper);
    }
}
