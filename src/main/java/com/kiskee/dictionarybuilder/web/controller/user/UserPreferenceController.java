package com.kiskee.dictionarybuilder.web.controller.user;

import com.kiskee.dictionarybuilder.model.dto.user.preference.DictionaryPreference;
import com.kiskee.dictionarybuilder.service.user.preference.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/preference")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping("/dictionary")
    public DictionaryPreference getDictionaryPreference() {
        return userPreferenceService.getDictionaryPreference();
    }
}
