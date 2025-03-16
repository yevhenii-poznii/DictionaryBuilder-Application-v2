package com.kiskee.dictionarybuilder.web.controller.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartRequest;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionServiceFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.message.RepetitionMessageHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.RepetitionStateHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/repetition")
public class RepetitionController {

    private final RepetitionServiceFactory repetitionService;

    @GetMapping("/running")
    public RepetitionRunningStatus isRepetitionRunning() {
        return repetitionService.execute(service -> ((RepetitionStateHandler) service).isRepetitionRunning());
    }

    @PostMapping("/{dictionaryId}")
    public RepetitionRunningStatus start(
            @PathVariable long dictionaryId, @RequestBody @Valid RepetitionStartRequest request) {
        return repetitionService.execute(
                request, service -> ((RepetitionStateHandler) service).start(dictionaryId, request));
    }

    @PutMapping("/pause")
    public RepetitionRunningStatus pause() {
        return repetitionService.execute(service -> ((RepetitionStateHandler) service).pause());
    }

    @PutMapping("/unpause")
    public RepetitionRunningStatus unpause() {
        return repetitionService.execute(service -> ((RepetitionStateHandler) service).unpause());
    }

    @DeleteMapping
    public RepetitionRunningStatus stop() {
        return repetitionService.execute(service -> ((RepetitionStateHandler) service).stop());
    }

    @MessageMapping("/handle")
    @SendToUser("/queue/response")
    public WSResponse handleMessage(Authentication authentication, WSRequest request) {
        return repetitionService.execute(request, service -> ((RepetitionMessageHandler) service)
                .handleRepetitionMessage(authentication, request));
    }
}
