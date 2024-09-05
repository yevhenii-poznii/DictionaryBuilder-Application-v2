package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import org.springframework.security.core.Authentication;

public interface RepetitionService extends CommonRepetitionService {

    RepetitionRunningStatus start(long dictionaryId, RepetitionStartFilterRequest request);

    WSResponse handleRepetitionMessage(Authentication principal, WSRequest request);
}
