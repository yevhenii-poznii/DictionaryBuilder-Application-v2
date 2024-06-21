package com.kiskee.vocabulary.service.vocabulary.repetition;

import com.kiskee.vocabulary.model.dto.repetition.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import org.springframework.security.core.Authentication;

public interface RepetitionService {

    void start(long dictionaryId, DictionaryPageRequestDto request);

    WSResponse check(Authentication principal, WSRequest request);
}
