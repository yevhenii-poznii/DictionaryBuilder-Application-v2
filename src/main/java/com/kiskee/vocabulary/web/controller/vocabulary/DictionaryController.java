package com.kiskee.vocabulary.web.controller.vocabulary;

import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryCreationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/dictionary")
public class DictionaryController {

    private final DictionaryCreationService dictionaryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DictionarySaveResponse createDictionary(@RequestBody @Valid DictionarySaveRequest dictionarySaveRequest) {
        return dictionaryService.addDictionary(dictionarySaveRequest);
    }

}
