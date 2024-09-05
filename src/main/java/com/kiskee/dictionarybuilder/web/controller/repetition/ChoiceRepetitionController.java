package com.kiskee.dictionarybuilder.web.controller.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.choice.ChoiceRepetitionHandler;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/repetition/choice")
public class ChoiceRepetitionController {

    private final ChoiceRepetitionHandler choiceRepetitionHandler;

    @PostMapping("/{dictionaryId}")
    public RepetitionRunningStatus start(
            @PathVariable long dictionaryId, @RequestBody @Valid RepetitionStartFilterRequest request) {
        return choiceRepetitionHandler.start(dictionaryId, request);
    }

    @MessageMapping("/handle/choice")
    @SendToUser("/queue/response")
    public WSResponse handleMessage(Authentication authentication, WSRequest request) {
        return choiceRepetitionHandler.handleRepetitionMessage(authentication, request);
    }
}
