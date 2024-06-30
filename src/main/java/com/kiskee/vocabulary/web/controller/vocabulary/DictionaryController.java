package com.kiskee.vocabulary.web.controller.vocabulary;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public DictionaryPageResponseDto getPage(
            @PathVariable Long dictionaryId, @ModelAttribute @Valid DictionaryPageRequestDto pageRequest) {
        return dictionaryService.getDictionaryPageByOwner(dictionaryId, pageRequest);
    }

    @GetMapping
    public List<DictionaryDto> getDictionaries() {
        return dictionaryService.getDictionaries();
    }

    @GetMapping("/detailed")
    public List<DictionaryDetailDto> getDetailedDictionaries() {
        return dictionaryService.getDetailedDictionaries();
    }

    @PutMapping("/{dictionaryId}")
    public DictionarySaveResponse updateDictionary(
            @PathVariable Long dictionaryId, @RequestBody @Valid DictionarySaveRequest dictionarySaveRequest) {
        return dictionaryService.updateDictionary(dictionaryId, dictionarySaveRequest);
    }

    @DeleteMapping
    public ResponseMessage deleteDictionaries(@RequestParam Set<Long> dictionaryIds) {
        System.out.println(dictionaryIds);
        // TODO implement
        return new ResponseMessage(String.format("Deleted %d dictionaries: %s", dictionaryIds.size(), dictionaryIds));
    }

    @DeleteMapping("/{dictionaryId}")
    public ResponseMessage deleteDictionary(@PathVariable Long dictionaryId) {
        return dictionaryService.deleteDictionary(dictionaryId);
    }
}
