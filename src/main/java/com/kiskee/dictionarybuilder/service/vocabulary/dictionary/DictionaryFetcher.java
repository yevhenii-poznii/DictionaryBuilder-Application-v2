package com.kiskee.dictionarybuilder.service.vocabulary.dictionary;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import java.util.List;

public interface DictionaryFetcher {

    List<DictionaryDto> getDictionaries();
}
