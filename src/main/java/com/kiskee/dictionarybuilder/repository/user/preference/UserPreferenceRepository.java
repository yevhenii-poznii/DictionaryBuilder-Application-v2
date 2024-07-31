package com.kiskee.dictionarybuilder.repository.user.preference;

import com.kiskee.dictionarybuilder.model.dto.user.preference.WordPreference;
import com.kiskee.dictionarybuilder.model.entity.user.preference.UserPreference;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    WordPreference findWordPreferenceByUserId(UUID userId);
}
