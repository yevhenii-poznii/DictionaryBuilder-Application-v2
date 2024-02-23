package com.kiskee.vocabulary.service.vocabulary.dictionary;

import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;

public interface DictionaryCreationService {

    Dictionary addDictionary(String dictionaryName);

    DictionarySaveResponse addDictionary(DictionarySaveRequest dictionarySaveRequest);

}
