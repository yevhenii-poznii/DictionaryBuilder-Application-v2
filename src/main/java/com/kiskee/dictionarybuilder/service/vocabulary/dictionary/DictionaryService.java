package com.kiskee.dictionarybuilder.service.vocabulary.dictionary;

import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import java.util.List;
import java.util.Set;

public interface DictionaryService extends DictionaryCreationService, DictionaryFetcher {

    DictionaryPageResponseDto getDictionaryPageByOwner(
            Long dictionaryId, DictionaryPageRequestDto dictionaryPageRequest);

    List<DictionaryDetailDto> getDetailedDictionaries();

    DictionarySaveResponse updateDictionary(Long dictionaryId, DictionarySaveRequest dictionarySaveRequest);

    ResponseMessage deleteDictionaries(Set<Long> dictionaryIds);

    ResponseMessage deleteDictionary(Long dictionaryId);
}
