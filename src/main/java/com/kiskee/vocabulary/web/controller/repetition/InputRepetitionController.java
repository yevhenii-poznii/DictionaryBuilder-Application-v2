package com.kiskee.vocabulary.web.controller.repetition;

import com.kiskee.vocabulary.model.dto.repetition.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.vocabulary.service.vocabulary.repetition.InputRepetitionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/repetition")
public class InputRepetitionController {

    private final InputRepetitionService inputRepetitionService;

    @PostMapping("/{dictionaryId}")
    public void start(@PathVariable long dictionaryId, @ModelAttribute @Valid DictionaryPageRequestDto request) {
        inputRepetitionService.start(dictionaryId, request);
    }

    @MessageMapping("/handle")
    @SendToUser("/queue/response")
    public WSResponse handleRepetitionMessage(Authentication authentication, WSRequest request) {
        return inputRepetitionService.check(authentication, request);
    }

    @PutMapping
    public void pause() {}

    @DeleteMapping
    public void stop() {}
}
