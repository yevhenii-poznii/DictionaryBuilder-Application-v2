package com.kiskee.vocabulary.service.vocabulary.repetition;

import com.kiskee.vocabulary.model.dto.repetition.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import java.security.Principal;

public interface RepetitionService {

    void start(long dictionaryId, DictionaryPageRequestDto request);

    WSResponse check(Principal principal, WSRequest request);
}
