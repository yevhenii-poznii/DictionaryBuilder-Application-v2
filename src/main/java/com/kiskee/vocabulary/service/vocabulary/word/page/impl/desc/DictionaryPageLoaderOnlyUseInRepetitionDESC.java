package com.kiskee.vocabulary.service.vocabulary.word.page.impl.desc;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryPageMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordIdDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.repository.vocabulary.WordRepository;
import com.kiskee.vocabulary.service.vocabulary.word.page.AbstractDictionaryPageLoaderDESC;
import com.kiskee.vocabulary.service.vocabulary.word.page.DictionaryPageLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public DictionaryPageLoaderOnlyUseInRepetitionDESC(WordRepository repository, DictionaryPageMapper mapper) {
        super(repository, mapper);
    }

}
