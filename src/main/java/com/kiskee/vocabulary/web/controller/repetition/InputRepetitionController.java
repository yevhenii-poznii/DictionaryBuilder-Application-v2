package com.kiskee.vocabulary.web.controller.repetition;

import com.kiskee.vocabulary.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.service.vocabulary.repetition.InputRepetitionService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/repetition")
public class InputRepetitionController {

    private final InputRepetitionService inputRepetitionService;

    @GetMapping("/running")
    public RepetitionRunningStatus isRepetitionRunning() {
        return inputRepetitionService.isRepetitionRunning();
    }

    @GetMapping("/test/{dictionaryId}")
    public List<WordDto> test(
            @PathVariable long dictionaryId, @RequestBody @Valid RepetitionStartFilterRequest request) {
        return inputRepetitionService.test(dictionaryId, request);
    }

    @PostMapping("/{dictionaryId}")
    public void start(@PathVariable long dictionaryId, @RequestBody @Valid RepetitionStartFilterRequest request) {
        inputRepetitionService.start(dictionaryId, request);
    }

    @MessageMapping("/handle")
    @SendToUser("/queue/response")
    public WSResponse handleMessage(Authentication authentication, WSRequest request) {
        return inputRepetitionService.handleRepetitionMessage(authentication, request);
    }

    @PutMapping
    public void pause() {}

    @DeleteMapping
    public void stop() {}
}
