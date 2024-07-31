package com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.desc;

import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.vocabulary.DictionaryPageRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.AbstractDictionaryPageLoaderDESC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DictionaryPageLoaderOnlyUseInRepetitionDESC extends AbstractDictionaryPageLoaderDESC
        implements DictionaryPageLoader {

    @Override
    protected Page<WordIdDto> findWordIdsPage(Long dictionaryId, Pageable pageable) {
        return getRepository().findByDictionaryIdAndUseInRepetition(dictionaryId, true, pageable);
    }

    @Override
    protected List<Word> loadWordsByFilter(List<Long> wordIds) {
        return getRepository().findByIdInAndUseInRepetitionOrderByAddedAtDesc(wordIds, true);
    }

    @Override
    public PageFilter getPageFilter() {
        return PageFilter.ONLY_USE_IN_REPETITION_DESC;
    }

    public DictionaryPageLoaderOnlyUseInRepetitionDESC(
            DictionaryPageRepository repository, DictionaryPageMapper mapper) {
        super(repository, mapper);
    }
}
