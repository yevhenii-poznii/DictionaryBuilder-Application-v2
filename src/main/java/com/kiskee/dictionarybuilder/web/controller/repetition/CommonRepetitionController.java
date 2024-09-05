package com.kiskee.dictionarybuilder.web.controller.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.CommonRepetitionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/repetition")
public class CommonRepetitionController {

    private final CommonRepetitionService inputRepetitionService;

    @GetMapping("/running")
    public RepetitionRunningStatus isRepetitionRunning() {
        return inputRepetitionService.isRepetitionRunning();
    }

    @PutMapping("/pause")
    public RepetitionRunningStatus pause() {
        return inputRepetitionService.pause();
    }

    @PutMapping("/unpause")
    public RepetitionRunningStatus unpause() {
        return inputRepetitionService.unpause();
    }

    @DeleteMapping
    public RepetitionRunningStatus stop() {
        return inputRepetitionService.stop();
    }
}
