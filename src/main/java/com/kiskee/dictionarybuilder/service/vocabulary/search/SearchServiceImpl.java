package com.kiskee.dictionarybuilder.service.vocabulary.search;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.repository.vocabulary.SearchWordRepository;
import com.kiskee.dictionarybuilder.repository.vocabulary.projections.WordProjection;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryFetcher;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final DictionaryFetcher dictionaryFetcher;
    private final SearchWordRepository repository;

    @Override
    @Transactional
    public List<WordProjection> search(String searchWord) {
        List<DictionaryDto> dictionaries = dictionaryFetcher.getDictionaries();
        Set<Long> dictionaryIds =
                dictionaries.stream().map(DictionaryDto::getId).collect(Collectors.toSet());
        return repository.findByWordContainsIgnoreCaseAndDictionaryIdIn(searchWord, dictionaryIds);
    }
}
