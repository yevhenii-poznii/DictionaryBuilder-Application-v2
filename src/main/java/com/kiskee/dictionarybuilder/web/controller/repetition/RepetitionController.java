package com.kiskee.dictionarybuilder.web.controller.repetition;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/repetition")
public class RepetitionController {

    private final RepetitionService repetitionService;

    @GetMapping("/running")
    public RepetitionRunningStatus isRepetitionRunning() {
        return repetitionService.isRepetitionRunning();
    }

    @PostMapping("/{dictionaryId}")
    public RepetitionRunningStatus start(
            @PathVariable long dictionaryId,
            @RequestParam RepetitionType repetitionType,
            @RequestBody @Valid RepetitionStartFilterRequest request) {
        return repetitionService.start(dictionaryId, repetitionType, request);
    }

    @PutMapping("/pause")
    public RepetitionRunningStatus pause() {
        return repetitionService.pause();
    }

    @PutMapping("/unpause")
    public RepetitionRunningStatus unpause() {
        return repetitionService.unpause();
    }

    @DeleteMapping
    public RepetitionRunningStatus stop() {
        return repetitionService.stop();
    }

    @MessageMapping("/handle")
    @SendToUser("/queue/response")
    public WSResponse handleMessage(Authentication authentication, WSRequest request) {
        return repetitionService.handleRepetitionMessage(authentication, request);
    }
}
