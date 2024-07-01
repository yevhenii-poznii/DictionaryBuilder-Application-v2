package com.kiskee.vocabulary.service.vocabulary.dictionary;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import java.util.List;
import java.util.Set;

public interface DictionaryService extends DictionaryCreationService {

    DictionaryPageResponseDto getDictionaryPageByOwner(
            Long dictionaryId, DictionaryPageRequestDto dictionaryPageRequest);

    List<DictionaryDto> getDictionaries();

    List<DictionaryDetailDto> getDetailedDictionaries();

    DictionarySaveResponse updateDictionary(Long dictionaryId, DictionarySaveRequest dictionarySaveRequest);

    ResponseMessage deleteDictionaries(Set<Long> dictionaryIds);

    ResponseMessage deleteDictionary(Long dictionaryId);
}
