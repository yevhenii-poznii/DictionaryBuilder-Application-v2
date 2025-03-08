package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRequest;

public interface RepetitionHandler {

    Class<? extends RepetitionRequest> getRequestType();
}
