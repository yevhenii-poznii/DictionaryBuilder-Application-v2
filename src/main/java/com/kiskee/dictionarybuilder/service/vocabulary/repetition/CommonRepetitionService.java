package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;

public interface CommonRepetitionService {

    RepetitionRunningStatus isRepetitionRunning();

    RepetitionRunningStatus pause();

    RepetitionRunningStatus unpause();

    RepetitionRunningStatus stop();
}
