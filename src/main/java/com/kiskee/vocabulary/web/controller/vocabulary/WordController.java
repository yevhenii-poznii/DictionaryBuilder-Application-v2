package com.kiskee.vocabulary.web.controller.vocabulary;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordUpdateRequest;
import com.kiskee.vocabulary.service.vocabulary.word.WordService;
import jakarta.validation.Valid;
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

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/dictionaries/{dictionaryId}/words")
public class WordController {

    private final WordService wordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WordSaveResponse add(@PathVariable Long dictionaryId,
                                @RequestBody @Valid WordSaveRequest wordSaveRequest) {
        return wordService.addWord(dictionaryId, wordSaveRequest);
    }

    @PutMapping("/{wordId}")
    public WordSaveResponse update(@PathVariable Long dictionaryId,
                                   @PathVariable Long wordId,
                                   @RequestBody @Valid WordUpdateRequest wordUpdateRequest) {
        return wordService.updateWord(dictionaryId, wordId, wordUpdateRequest);
    }

    @DeleteMapping("/{wordId}")
    public ResponseMessage delete(@PathVariable Long dictionaryId,
                                  @PathVariable Long wordId) {
        return wordService.deleteWord(dictionaryId, wordId);
    }

    @DeleteMapping
    public ResponseMessage deleteSet(@PathVariable Long dictionaryId,
                                     @RequestParam Set<Long> wordIds) {
        return wordService.deleteWords(dictionaryId, wordIds);
    }

}
