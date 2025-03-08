package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartRequest;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.RepetitionHandler;

public interface RepetitionStateHandler extends RepetitionHandler {

    RepetitionRunningStatus isRepetitionRunning();

    RepetitionRunningStatus start(long dictionaryId, RepetitionStartRequest request);

    RepetitionRunningStatus pause();

    RepetitionRunningStatus unpause();

    RepetitionRunningStatus stop();
}
