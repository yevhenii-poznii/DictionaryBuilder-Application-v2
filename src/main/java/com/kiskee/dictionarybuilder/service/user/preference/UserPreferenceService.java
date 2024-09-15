package com.kiskee.dictionarybuilder.service.user.preference;

import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceOptionsDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.dictionary.DictionaryPreferenceOptionDto;

public interface UserPreferenceService {

    UserPreferenceOptionsDto getUserPreference();

    UserPreferenceDto updateUserPreference(UserPreferenceDto userPreferenceDto);

    DictionaryPreferenceOptionDto getDictionaryPreference();
}
