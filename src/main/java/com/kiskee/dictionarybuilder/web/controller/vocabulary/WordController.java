package com.kiskee.dictionarybuilder.web.controller.vocabulary;

import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordSaveResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordSaveUpdateRequest;
import com.kiskee.dictionarybuilder.service.vocabulary.word.WordService;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/dictionaries/{dictionaryId}/words")
public class WordController {

    private final WordService wordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WordSaveResponse add(
            @PathVariable Long dictionaryId, @RequestBody @Valid WordSaveUpdateRequest wordSaveRequest) {
        return wordService.addWord(dictionaryId, wordSaveRequest);
    }

    @PutMapping("/{wordId}")
    public WordSaveResponse update(
            @PathVariable Long dictionaryId,
            @PathVariable Long wordId,
            @RequestBody @Valid WordSaveUpdateRequest wordUpdateRequest) {
        return wordService.updateWord(dictionaryId, wordId, wordUpdateRequest);
    }

    @PutMapping("/{wordId}/repetition")
    public ResponseMessage updateRepetition(
            @PathVariable Long dictionaryId, @PathVariable Long wordId, @RequestParam Boolean useInRepetition) {
        return wordService.updateRepetition(dictionaryId, wordId, useInRepetition);
    }

    @DeleteMapping("/{wordId}")
    public ResponseMessage delete(@PathVariable Long dictionaryId, @PathVariable Long wordId) {
        return wordService.deleteWord(dictionaryId, wordId);
    }

    @DeleteMapping
    public ResponseMessage deleteSet(@PathVariable Long dictionaryId, @RequestParam Set<Long> wordIds) {
        return wordService.deleteWords(dictionaryId, wordIds);
    }

    @PutMapping("/{wordId}/move")
    public ResponseMessage moveWord(
            @PathVariable Long dictionaryId, @PathVariable Long wordId, @RequestParam Long targetDictionaryId) {
        return wordService.moveWord(dictionaryId, wordId, targetDictionaryId);
    }
}
