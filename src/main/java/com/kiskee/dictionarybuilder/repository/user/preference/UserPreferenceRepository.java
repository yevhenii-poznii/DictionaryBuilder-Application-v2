package com.kiskee.dictionarybuilder.repository.user.preference;

import com.kiskee.dictionarybuilder.model.dto.user.preference.DictionaryPreference;
import com.kiskee.dictionarybuilder.model.dto.user.preference.ProfilePreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.model.dto.user.preference.WordPreference;
import com.kiskee.dictionarybuilder.model.entity.user.preference.UserPreference;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    WordPreference findWordPreferenceByUserId(UUID userId);

    UserPreferenceDto findUserPreferenceByUserId(UUID userId);

    DictionaryPreference findDictionaryPreferenceByUserId(UUID userId);

    ProfilePreferenceDto findProfileVisibilityByUserId(UUID userId);
}
