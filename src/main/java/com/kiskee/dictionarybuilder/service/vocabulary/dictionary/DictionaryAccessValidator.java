package com.kiskee.dictionarybuilder.service.vocabulary.dictionary;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import java.util.UUID;

public interface DictionaryAccessValidator {

    void verifyUserHasDictionary(Long dictionaryId);

    DictionaryDto getDictionaryByIdAndUserId(Long dictionaryId, UUID userId);
}
