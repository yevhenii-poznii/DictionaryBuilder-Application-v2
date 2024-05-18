package com.kiskee.vocabulary.service.vocabulary.dictionary;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import java.util.List;

public interface DictionaryService extends DictionaryCreationService {

    DictionaryPageResponseDto getDictionaryPageByOwner(
            Long dictionaryId, DictionaryPageRequestDto dictionaryPageRequest);

    List<DictionaryDto> getDictionaries();

    DictionarySaveResponse updateDictionary(Long dictionaryId, DictionarySaveRequest dictionarySaveRequest);

    ResponseMessage deleteDictionary(Long dictionaryId);
}
