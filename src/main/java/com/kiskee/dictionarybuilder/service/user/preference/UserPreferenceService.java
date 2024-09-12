package com.kiskee.dictionarybuilder.service.user.preference;

import com.kiskee.dictionarybuilder.model.dto.user.preference.DictionaryPreference;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;

public interface UserPreferenceService {

    UserPreferenceDto getUserPreference();

    UserPreferenceDto updateUserPreference(UserPreferenceDto userPreferenceDto);

    DictionaryPreference getDictionaryPreference();
}
