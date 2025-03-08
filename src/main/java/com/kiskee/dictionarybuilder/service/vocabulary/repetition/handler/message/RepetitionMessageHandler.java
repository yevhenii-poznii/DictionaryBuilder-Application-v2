package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.message;

import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.RepetitionHandler;
import org.springframework.security.core.Authentication;

public interface RepetitionMessageHandler extends RepetitionHandler {

    WSResponse handleRepetitionMessage(Authentication principal, WSRequest request);
}
