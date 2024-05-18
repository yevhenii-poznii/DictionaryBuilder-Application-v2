package com.kiskee.vocabulary.service.vocabulary.word.page.impl.desc;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.vocabulary.DictionaryPageRepository;
import com.kiskee.vocabulary.service.vocabulary.word.page.AbstractDictionaryPageLoaderDESC;
import com.kiskee.vocabulary.service.vocabulary.word.page.DictionaryPageLoader;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DictionaryPageLoaderOnlyNotUseInRepetitionDESC extends AbstractDictionaryPageLoaderDESC
        implements DictionaryPageLoader {

    @Override
    protected Page<WordIdDto> findWordIdsPage(Long dictionaryId, Pageable pageable) {
        return getRepository().findByDictionaryIdAndUseInRepetition(dictionaryId, false, pageable);
    }

    @Override
    protected List<Word> loadWordsByFilter(List<Long> wordIds) {
        return getRepository().findByIdInAndUseInRepetitionOrderByAddedAtDesc(wordIds, false);
    }

    @Override
    public PageFilter getPageFilter() {
        return PageFilter.ONLY_NOT_USE_IN_REPETITION_DESC;
    }

    public DictionaryPageLoaderOnlyNotUseInRepetitionDESC(
            DictionaryPageRepository repository, DictionaryPageMapper mapper) {
        super(repository, mapper);
    }
}
