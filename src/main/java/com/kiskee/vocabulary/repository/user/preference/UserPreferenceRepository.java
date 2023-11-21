package com.kiskee.vocabulary.repository.user.preference;

import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import com.kiskee.vocabulary.repository.user.BaseUserProfilePreferenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository
        extends BaseUserProfilePreferenceRepository, JpaRepository<UserPreference, Long> {
}
