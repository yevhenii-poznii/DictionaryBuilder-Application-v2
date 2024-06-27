package com.kiskee.vocabulary.service.vocabulary.repetition;

import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStatusResponse;
import com.kiskee.vocabulary.model.dto.repetition.message.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import org.springframework.security.core.Authentication;

public interface RepetitionService {

    RepetitionStatusResponse isRepetitionRunning();

    void start(long dictionaryId, RepetitionStartFilterRequest request);

    WSResponse handleRepetitionMessage(Authentication principal, WSRequest request);
}
