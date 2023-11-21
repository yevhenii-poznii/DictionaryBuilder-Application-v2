package com.kiskee.vocabulary.service.vocabulary;

import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.vocabulary.DictionaryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

    private final DictionaryRepository repository;

    @Override
    public Dictionary createEmptyDictionary(String dictionaryName) {
        Dictionary dictionary = new Dictionary(null, dictionaryName, List.of());

        return repository.save(dictionary);
    }

}
