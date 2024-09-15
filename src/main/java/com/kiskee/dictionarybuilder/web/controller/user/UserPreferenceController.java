package com.kiskee.dictionarybuilder.web.controller.user;

import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceOptionsDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary.DictionaryPreferenceOptionDto;
import com.kiskee.dictionarybuilder.service.user.preference.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/preference")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public UserPreferenceOptionsDto getUserPreference() {
        return userPreferenceService.getUserPreference();
    }

    @PutMapping
    public UserPreferenceDto updateUserPreference(@RequestBody @Valid UserPreferenceDto userPreferenceDto) {
        return userPreferenceService.updateUserPreference(userPreferenceDto);
    }

    @GetMapping("/dictionary")
    public DictionaryPreferenceOptionDto getDictionaryPreference() {
        return userPreferenceService.getDictionaryPreference();
    }
}
