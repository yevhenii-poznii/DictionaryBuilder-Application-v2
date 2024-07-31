package com.kiskee.dictionarybuilder.service.vocabulary.dictionary;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;

public interface DictionaryCreationService {

    Dictionary addDictionary(String dictionaryName);

    DictionarySaveResponse addDictionary(DictionarySaveRequest dictionarySaveRequest);
}
