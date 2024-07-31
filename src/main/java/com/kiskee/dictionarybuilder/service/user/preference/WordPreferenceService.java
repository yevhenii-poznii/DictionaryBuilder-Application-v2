package com.kiskee.dictionarybuilder.service.user.preference;

import com.kiskee.dictionarybuilder.model.dto.user.preference.WordPreference;
import java.util.UUID;

public interface WordPreferenceService {

    WordPreference getWordPreference(UUID userId);
}
