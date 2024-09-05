package com.kiskee.dictionarybuilder.web.controller.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.input.InputRepetitionHandler;
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
@RequestMapping("/repetition/input")
public class InputRepetitionController {

    private final InputRepetitionHandler inputRepetitionHandler;

    @PostMapping("/{dictionaryId}")
    public RepetitionRunningStatus start(
            @PathVariable long dictionaryId, @RequestBody @Valid RepetitionStartFilterRequest request) {
        return inputRepetitionHandler.start(dictionaryId, request);
    }

    @MessageMapping("/handle/input")
    @SendToUser("/queue/response")
    public WSResponse handleMessage(Authentication authentication, WSRequest request) {
        return inputRepetitionHandler.handleRepetitionMessage(authentication, request);
    }
}
