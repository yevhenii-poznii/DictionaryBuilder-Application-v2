package com.kiskee.dictionarybuilder.service.vocabulary.search;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.repository.vocabulary.SearchWordRepository;
import com.kiskee.dictionarybuilder.repository.vocabulary.projections.WordProjection;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryFetcher;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final DictionaryFetcher dictionaryFetcher;
    private final SearchWordRepository repository;

    @Override
    @Transactional
    public List<WordProjection> search(String searchWord) {
        if (Objects.isNull(searchWord) || searchWord.isBlank()) {
            throw new ValidationException("Search word cannot be null or empty");
        }
        List<DictionaryDto> dictionaries = dictionaryFetcher.getDictionaries();
        Set<Long> dictionaryIds =
                dictionaries.stream().map(DictionaryDto::getId).collect(Collectors.toSet());
        List<WordProjection> foundWords =
                repository.findByWordContainsIgnoreCaseAndDictionaryIdIn(searchWord, dictionaryIds);
        log.info(
                "Found {} words for search word: {} for user: {}",
                foundWords.size(),
                searchWord,
                IdentityUtil.getUserId());
        return foundWords;
    }
}
