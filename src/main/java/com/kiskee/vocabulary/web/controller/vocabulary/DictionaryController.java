package com.kiskee.vocabulary.web.controller.vocabulary;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/dictionaries")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DictionarySaveResponse createDictionary(@RequestBody @Valid DictionarySaveRequest dictionarySaveRequest) {
        return dictionaryService.addDictionary(dictionarySaveRequest);
    }

    @GetMapping("/{dictionaryId}")
    public DictionaryPageResponseDto getPage(@PathVariable Long dictionaryId,
                                             @ModelAttribute @Valid DictionaryPageRequestDto pageRequest) {
        return dictionaryService.getDictionaryPageByOwner(dictionaryId, pageRequest);
    }

    @GetMapping
    public List<DictionaryDto> getDictionaries() {
        return dictionaryService.getDictionaries();
    }

    @PutMapping("/{dictionaryId}")
    public DictionarySaveResponse updateDictionary(@PathVariable Long dictionaryId,
                                                   @RequestBody @Valid DictionarySaveRequest dictionarySaveRequest) {
        return dictionaryService.updateDictionary(dictionaryId, dictionarySaveRequest);
    }

    @DeleteMapping("/{dictionaryId}")
    public ResponseMessage deleteDictionary(@PathVariable Long dictionaryId) {
        return dictionaryService.deleteDictionary(dictionaryId);
    }

}
