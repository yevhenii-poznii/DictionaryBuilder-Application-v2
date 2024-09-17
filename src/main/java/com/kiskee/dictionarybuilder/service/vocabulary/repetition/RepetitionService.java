package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import org.springframework.security.core.Authentication;

public interface RepetitionService {

    RepetitionRunningStatus isRepetitionRunning();

    RepetitionRunningStatus start(
            long dictionaryId, RepetitionType repetitionType, RepetitionStartFilterRequest request);

    RepetitionRunningStatus pause();

    RepetitionRunningStatus unpause();

    RepetitionRunningStatus stop();

    WSResponse handleRepetitionMessage(Authentication principal, WSRequest request);
}
