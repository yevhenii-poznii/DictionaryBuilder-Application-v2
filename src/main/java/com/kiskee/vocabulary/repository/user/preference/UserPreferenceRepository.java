package com.kiskee.vocabulary.repository.user.preference;

import com.kiskee.vocabulary.model.entity.user.preference.UserPreference;
import com.kiskee.vocabulary.repository.user.BaseUserProfilePreferenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPreferenceRepository
        extends BaseUserProfilePreferenceRepository, JpaRepository<UserPreference, UUID> {
}
